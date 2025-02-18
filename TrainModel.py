import json
import torch
from torch.optim import AdamW
from torch.utils.data import Dataset, DataLoader
from transformers import LongformerTokenizer, LongformerModel

class QA_Dataset(Dataset):
    def __init__(self, questions, answers, questions_amount):
        self.questions = questions
        self.answers = answers
        self.questions_amount = questions_amount
        
    def __len__(self):
        return len(self.questions)
    
    def __getitem__(self, idx):
        iterator = idx * self.questions_amount + 1
        tokenized_questions = []

        for i in range(27):
            tokenized_questions.append(self.questions[iterator])
            iterator += 1

        return {
            'input_ids': tokenized_questions,
            'labels': self.answers[idx]
        }
    
# Load database
data_file = "QA_database_EN.json"
questions = []
answers = []

start = 0
limit = 700000
iterator = 0
with open(data_file, 'r') as file:
    for line in file:
        if line.strip():
            if iterator >= start:
                data = json.loads(line)
                questions.append(data.get('Question'))
                print(questions[0])
                answers.append(data.get('Answer'))

            iterator += 1
            if iterator >= limit:
                break

print("Arrays are ready!")

# Tokenize data
max_length = 768
tokenizer = LongformerTokenizer.from_pretrained('allenai/longformer-base-4096')

tokenized_questions = [tokenizer.encode(q, max_length=max_length, padding="max_length", truncation=True, return_tensors='pt') for question in questions for q in question]
print("Questions has been tokenized!")

tokenized_answers = [tokenizer.encode(a, max_length=max_length, padding="max_length", truncation=True, return_tensors='pt') for a in answers]
print("Answers has been tokenized!")

# Create a Dataset and DataLoader
questions_amount = len(questions[0])
dataset = QA_Dataset(tokenized_questions, tokenized_answers, questions_amount)
dataloader = DataLoader(dataset, batch_size=4, shuffle=True)

# Load model
model = LongformerModel.from_pretrained('allenai/longformer-base-4096')
model.config.hidden_size = max_length
model.resize_token_embeddings(len(tokenizer))

# Optimizer
optimizer = AdamW(model.parameters(), lr=5e-5, weight_decay=0.01)

# Check if CUDA is available, and move tensor to GPU if so
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
model.to(device)

# Training loop
epochs = 1
for epoch in range(epochs):
    model.train()
    total_loss = 0
    for batch in dataloader:
        optimizer.zero_grad()

        # Get input data and move them to GPU
        input_ids = batch['input_ids'].to(device)
        attention_mask = batch['attention_mask'].to(device)
        labels = batch['labels'].to(device)

        # Forward pass
        outputs = model(input_ids=input_ids, attention_mask=attention_mask, labels=labels)
        loss = outputs.loss
        total_loss += loss.item()

        # Backward pass and optimize
        loss.backward()
        optimizer.step()

# Save model and a tokenizer
tokenizer.save_pretrained('my_model')
model.save_pretrained('my_model')

print("Done")
