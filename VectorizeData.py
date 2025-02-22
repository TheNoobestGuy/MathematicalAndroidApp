import json
import pickle
from transformers import T5Tokenizer

# Load database
data_file = "QA_database_EN.json"

# Load the tokenizer
tokenizer = T5Tokenizer.from_pretrained('t5-large')

questions = []
answers = []

with open(data_file, 'r') as file:
    for line in file:
        if line.strip():
            data = json.loads(line)

            list = data.get('Questions')

            questions.append([])
            for idx, question in enumerate(list):
                questions[-1].append(question)
                    
            answers.append(data.get('Answer'))

print("Arrays are ready!")

# Tokenize data
with open('vectorized_questions.pkl', 'ab') as f:
    for question in questions:
            for q in question:
                vector = tokenizer.encode(q, return_tensors='pt')
                pickle.dump(vector, f)
                del vector

       
print("Questions has been tokenized!")

with open('vectorized_answers.pkl', 'ab') as f:
    for a in answers:
        vector = tokenizer.encode(a, return_tensors='pt')
        pickle.dump(vector, f)
        del vector

print("Answers has been tokenized!")