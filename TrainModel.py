import torch
import pickle
import gzip
from torch.optim import AdamW
from transformers import LongformerModel

# Load model
dimensions = 612
model = LongformerModel.from_pretrained('allenai/longformer-base-4096')
model.config.hidden_size = dimensions
model.resize_token_embeddings(dimensions)

# Optimizer
optimizer = AdamW(model.parameters(), lr=5e-5, weight_decay=0.01)

# Check if CUDA is available, and move tensor to GPU if so
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
model.to(device)

# Training loop
tokenized_questions_path = ""
tokenized_answers_path = ""

with gzip.open(tokenized_answers_path, 'rt', encoding="utf-8") as tkn_answers:
    with gzip.open(tokenized_questions_path, 'rt', encoding="utf-8") as tkn_questions:
        for epoch in range(2):
            model.train()
            total_loss = 0

            while True:
                try:
                    optimizer.zero_grad()
                    
                    # Get data from pickle files
                    answer = pickle.load(tkn_answers)
                    questions = []
                    attention_mask = []

                    for _ in range(6):
                        item = pickle.load(tkn_questions)
                        questions.append(item)
                        attention_mask.append(item['attention_mask'])

                    # Get input data and move them to GPU
                    input_ids = questions.to(device)
                    attention_mask = attention_mask.to(device)
                    labels = answer.to(device)

                     # Forward pass
                    outputs = model(input_ids=input_ids, attention_mask=attention_mask, labels=labels)
                    loss = outputs.loss
                    total_loss += loss.item()

                    # Backward pass and optimize
                    loss.backward()
                    optimizer.step()
                except:
                    break
            
            # Save model and free cache
            torch.save(model.state_dict(), 'model.pth')
            torch.cuda.empty_cache()

print("Done")
