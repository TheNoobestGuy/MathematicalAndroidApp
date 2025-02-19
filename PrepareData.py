import json
import gzip
import xml.etree.ElementTree as ET

data_file = "enwiki-latest-abstract.xml.gz"
output_file = "QA_database_EN.json"

with gzip.open(data_file, 'rt', encoding='utf-8') as f:
    with open(output_file, 'w', encoding="utf-8") as output:
        context = ET.iterparse(f, events=('start', 'end'))
        _, root = next(context)

        for event, elem in context:
            if event == 'end' and elem.tag == 'doc':
                subject = elem.find("title").text
                answer = elem.find("abstract").text

                if subject and answer:
                    subject = subject.replace("Wikipedia: ", "")

                    questions = [
                        f"{subject}",
                        f"What is {subject}?",
                        f"What does {subject} mean?",
                        f"Who is {subject}?",
                        f"What can you tell me about {subject}?",
                        f"Explain to me {subject}.",
                    ]

                    dataset = {
                        "Subject": subject,
                        "Questions": questions,
                        "Answer": answer
                    }

                    json.dump(dataset, output, ensure_ascii=False)
                    output.write("\n")

                    elem.clear()
