import os
import torch
import pickle
import gzip
import time
import traceback
from torch.optim import AdamW
from transformers import T5ForConditionalGeneration
import gc

# Class for dataloader
class QADataset():
    def __init__(self, answers, questions, questions_attention_masks, answers_attention_masks, batch_size):
        self.answers = answers
        self.questions = questions
        self.questions_attention_masks = questions_attention_masks
        self.answers_attention_masks = answers_attention_masks
        self.batch_size = batch_size

    def __iter__(self):
        index = 0
        while index < batch_size:
            yield self.__getitem__(index)
            index += 1

    def __getitem__(self, index):
        input_ids = torch.stack(self.questions[index]).squeeze(0)
        attention_mask = torch.stack(self.questions_attention_masks[index]).squeeze(0)

        return  {
            "input_ids": input_ids.squeeze(1),
            "attention_mask": attention_mask.squeeze(1),
            "labels": self.answers[index].repeat(self.batch_size, 1),
            "decoder_attention_mask": self.answers_attention_masks[index].repeat(self.batch_size, 1)
        }
        

# Check if CUDA is available, and move tensor to GPU if so
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

# Training variables
tokenized_questions_path = "vectorized_questions.pkl.gz"
tokenized_answers_path = "vectorized_answers.pkl.gz"
dimension = 768
batch_size = 1

# Epoches variables
start_epoch = 0
epoches = 140
step = 50000
limit = 0
questions_amount = 6

# Training loop
with gzip.open(tokenized_answers_path, 'rb') as tkn_answers:
    with gzip.open(tokenized_questions_path, 'rb') as tkn_questions:
        for epoch in range(epoches):
            # Load model
            if epoch >= start_epoch:
                model = T5ForConditionalGeneration.from_pretrained(f"my_model_{epoch}").to(device)
                model.gradient_checkpointing_enable()

                # Disable caching during training
                model.config.use_cache = False

                # Optimizer
                optimizer = AdamW(model.parameters(), lr=2e-5, weight_decay=0.001)
                model.train()

                print(f"Start, Epoch: {epoch}, Time: {time.strftime("%H:%M:%S")}")

            iterator = step * epoch
            limit += step

            while iterator < limit:
                # Ommit done records
                if start_epoch > epoch:
                    tokenized_answer = pickle.load(tkn_answers)
                    for question in range(questions_amount):
                        tokenized_question = pickle.load(tkn_questions)
                        del tokenized_question
                    del tokenized_answer
                    iterator += 1
                    continue

                try:
                    # Batches for datasets
                    answers_batch = []
                    anwser_attention_mask_batch = []
                    questions_batch = []
                    questions_attention_masks_batch = []

                    # Proccess files with vectorized data
                    for num in range(batch_size):
                        # Get answer input data from pickle file
                        tokenized_answer = pickle.load(tkn_answers).to(device)
                        answer = tokenized_answer

                        # Get questions input data from pickle file
                        current_questions_batch = []
                        current_questions_attention_masks_batch = []
                        
                        seq_len = 0
                        tokenized_question_size = 0
                        for question in range(questions_amount):
                            tokenized_question = pickle.load(tkn_questions).to(device)
                            seq_len = max(seq_len, answer.shape[1], tokenized_question.shape[1])
                            if tokenized_question.shape[1] > tokenized_question_size:
                                tokenized_question_size = tokenized_question.shape[1]
                            current_questions_batch.append(tokenized_question)
                            del tokenized_question

                        # Equalize tensors
                        if seq_len > dimension:
                            if tokenized_answer.shape[1] > dimension and tokenized_question_size > dimension:
                                anwser = tokenized_answer[:, :dimension]
                                for i in range(questions_amount):
                                    current_questions_batch[i] = current_questions_batch[i][:, :dimension]
                            elif seq_len == tokenized_answer.shape[1]:
                                anwser = tokenized_answer[:, :dimension]
                                for i in range(questions_amount):
                                    old_tensor = current_questions_batch[i]
                                    current_questions_batch[i] = torch.nn.functional.pad(current_questions_batch[i], (0, dimension-current_questions_batch[i].shape[1]), value=0)
                                    del old_tensor
                            else:
                                answer = torch.nn.functional.pad(tokenized_answer, (0, dimension-tokenized_answer.shape[1]), value=0)
                                for i in range(questions_amount):
                                    current_questions_batch[i] = current_questions_batch[i][:, :dimension]
                        else:
                            answer = torch.nn.functional.pad(tokenized_answer, (0, seq_len-tokenized_answer.shape[1]), value=0)
                            for i in range(questions_amount):
                                old_tensor = current_questions_batch[i]
                                current_questions_batch[i] = torch.nn.functional.pad(old_tensor, (0, seq_len-old_tensor.shape[1]), value=0)
                                del old_tensor
                                
                        # Make attention masks
                        for question in current_questions_batch:
                            current_questions_attention_masks_batch.append((question != 0).int())
                        current_answer_attention_mask = (answer != 0).int()
                        
                        # Push everything to proper batches
                        answers_batch.append(answer)
                        anwser_attention_mask_batch.append(current_answer_attention_mask)
                        questions_batch.append(current_questions_batch)
                        questions_attention_masks_batch.append(current_questions_attention_masks_batch)
                        
                        # Free memory
                        del answer
                        del current_questions_batch
                        del current_answer_attention_mask
                        del current_questions_attention_masks_batch

                    # Load the data with custom function
                    dataset = QADataset(answers_batch, questions_batch, questions_attention_masks_batch, anwser_attention_mask_batch, questions_amount)

                    # Train model
                    for batch in dataset:
                        # Get data from batch
                        input_ids = batch["input_ids"].squeeze(0)
                        attention_mask = batch["attention_mask"].squeeze(0)
                        decoder_attention_mask = batch["decoder_attention_mask"].squeeze(0)
                        labels = batch["labels"].squeeze(0)

                        # Forward pass through the model
                        outputs = model(
                            input_ids=input_ids, 
                            attention_mask=attention_mask, 
                            decoder_attention_mask=decoder_attention_mask,
                            labels=labels,
                            past_key_values = None
                            )
                            
                            # Handle loss
                        if outputs.loss is not None:
                            loss = outputs.loss
                            loss.backward()

                            optimizer.step()
                            optimizer.zero_grad()
                        del input_ids
                        del attention_mask
                        del decoder_attention_mask
                        del labels

                    # Free memory
                    del tokenized_answer
                    del answers_batch
                    del anwser_attention_mask_batch
                    del questions_batch
                    del questions_attention_masks_batch

                except Exception as e:
                    torch.cuda.empty_cache()
                    tb = traceback.extract_tb(e.__traceback__)
                    filename, lineno, func, text = tb[-1]
                    print(f"Exception in {filename}, line {lineno}, in {func}")
                    print(f"Code: {text}")
                    print(f"Error: {e}, Epoch: {epoch}")
                    break
                
                iterator += 1

                # Print time and save model
                if iterator % 1000 == 0:
                    torch.cuda.empty_cache()

            if epoch >= start_epoch:   
                # Save model
                model.save_pretrained(f"my_model_{epoch+1}")
                print(f"End, Epoch: {epoch}, Time: {time.strftime("%H:%M:%S")}")

                # Free memory
                del model
                torch.cuda.empty_cache()
                gc.collect()

# Done
print(f"Done, End time: {time.strftime("%H:%M:%S")}")