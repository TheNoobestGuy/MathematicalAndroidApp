import os
import torch
import pickle
import gzip
import time
import traceback
from torch.optim import AdamW
from torch.utils.data import Dataset, DataLoader
from transformers import T5ForConditionalGeneration

# Check if CUDA is available, and move tensor to GPU if so
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

# Class for dataloader
class QADataset(Dataset):
    def __init__(self, answers, questions, questions_attention_masks, answers_attention_masks):
        self.answers = answers
        self.questions = questions
        self.questions_attention_masks = questions_attention_masks
        self.answers_attention_masks = answers_attention_masks

    def __len__(self):
        return len(self.questions)

    def __getitem__(self, index):
        input_ids = torch.stack(self.questions[index]).squeeze(0)
        attention_mask = torch.stack(self.questions_attention_masks[index]).squeeze(0)

        batch_size = input_ids.shape[0]
        seq_len = input_ids.shape[2]

        return  {
            "input_ids": input_ids.reshape(batch_size, seq_len),
            "attention_mask": attention_mask.reshape(batch_size, seq_len),
            "labels": self.answers[index].repeat(batch_size, 1),
            "decoder_attention_mask": self.answers_attention_masks[index].repeat(batch_size, 1)
        }
        

# Load model
model = T5ForConditionalGeneration.from_pretrained('my_model').to(device)

# Disable caching during training
model.config.use_cache = False

# Disable fragmentation
os.environ["PYTORCH_CUDA_ALLOC_CONF"] = "expandable_segments:True"

# Optimizer
optimizer = AdamW(model.parameters(), lr=5e-5, weight_decay=0.01)

# Training variables
tokenized_questions_path = "vectorized_questions.pkl.gz"
tokenized_answers_path = "vectorized_answers.pkl.gz"
dimension = 768
batch_size = 1

# Error handling
start = 0
errors = []
error_index = 0

# Eopoches variables
epoches = 1
limit = 7000000
questions_amount = 6
accumulation_steps = 5

# Training loop
for epoch in range(epoches):
    with gzip.open(tokenized_answers_path, 'rb') as tkn_answers:
        with gzip.open(tokenized_questions_path, 'rb') as tkn_questions:

            model.train()
            iterator = 0

            print(f"Start, Time: {time.strftime("%H:%M:%S")}")

            while iterator < limit:
                # Repeat errors and ommit done records
                if start > iterator and not (error_index < len(errors) and iterator == errors[error_index]):
                    tokenized_answer = pickle.load(tkn_answers)
                    tokenized_question = pickle.load(tkn_questions)

                    del tokenized_answer
                    del tokenized_question

                    iterator += 1
                    continue
                elif error_index < len(errors) and iterator == errors[error_index]:
                    error_index += 1

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

                        for question in range(questions_amount):
                            tokenized_question = pickle.load(tkn_questions).to(device)
                            current_questions_batch.append(tokenized_question)
                            del tokenized_question

                        # Equalize tensors
                        dim = 0
                        size = 0
                        equal = True
                        tokenized_question_size = 0
                        for question in current_questions_batch:
                            if answer.shape[1] != question.shape[1]:
                                dim = max(answer.shape[1], question.shape[1])
                                equal = False
                            if dim > size:
                                size = dim
                            if question.shape[1] > tokenized_question_size:
                                tokenized_question_size = question.shape[1]
                        
                        if not equal:
                            if size > dimension:
                                if tokenized_answer.shape[1] > dimension and tokenized_question_size > dimension:
                                    anwser = tokenized_answer[:, :dimension]
                                    for i in range(questions_amount):
                                        current_questions_batch[i] = current_questions_batch[i][:, :dimension]
                                elif size == tokenized_answer.shape[1]:
                                    anwser = tokenized_answer[:, :dimension]
                                    for i in range(questions_amount):
                                        current_questions_batch[i] = torch.nn.functional.pad(current_questions_batch[i], (0, dimension-current_questions_batch[i].shape[1]), value=0)
                                else:
                                    answer = torch.nn.functional.pad(tokenized_answer, (0, dimension-tokenized_answer.shape[1]), value=0)
                                    for i in range(questions_amount):
                                        current_questions_batch[i] = current_questions_batch[i][:, :dimension]
                            else:
                                answer = torch.nn.functional.pad(tokenized_answer, (0, size-tokenized_answer.shape[1]), value=0)
                                for i in range(questions_amount):
                                    current_questions_batch[i] = torch.nn.functional.pad(current_questions_batch[i], (0, size-current_questions_batch[i].shape[1]), value=0)

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
                    dataset = QADataset(answers_batch, questions_batch, questions_attention_masks_batch, anwser_attention_mask_batch)
                    train_loader = DataLoader(dataset, batch_size=1)

                    # Train model
                    for batch in train_loader:
                        # Move the data to the GPU
                        input_ids = batch["input_ids"].to(device).squeeze(0)
                        attention_mask = batch["attention_mask"].to(device).squeeze(0)
                        decoder_attention_mask = batch["decoder_attention_mask"].to(device).squeeze(0)
                        labels = batch["labels"].to(device).squeeze(0)
                    
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

                            # Accumulate gradients
                            if (iterator + 1) % accumulation_steps == 0:
                                optimizer.step()
                                optimizer.zero_grad()

                    # Free memory
                    del tokenized_answer
                    del questions_batch
                    del questions_attention_masks_batch

                except Exception as e:
                    torch.cuda.empty_cache()
                    tb = traceback.extract_tb(e.__traceback__)
                    filename, lineno, func, text = tb[-1]
                    print(f"Exception in {filename}, line {lineno}, in {func}")
                    print(f"Code: {text}")
                    print(f"Error: {e}, Iteration: {iterator}")

                iterator += 1

                # Print time and save model
                if iterator > start:
                    if iterator % 10000 == 0:
                        print(f"Iteration: {iterator}, Time: {time.strftime("%H:%M:%S")}")
                    if iterator % 100000 == 0:
                        print(f"Iteration: {iterator}, Time: {time.strftime("%H:%M:%S")}")
                        model.save_pretrained(f"my_model_{iterator/100000}")

# Done
print(f"Done, End time: {time.strftime("%H:%M:%S")}")
model.save_pretrained(f"my_model")