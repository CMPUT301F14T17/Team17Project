curl -XDELETE "http://estest.michaelblouin.com:9182/moqatest"
curl -XPUT "http://estest.michaelblouin.com:9182/moqatest"
curl -XPOST "http://estest.michaelblouin.com:9182/_bulk?pretty" --data-binary @bulk_questions.txt
curl -XPOST "http://estest.michaelblouin.com:9182/_bulk?pretty" --data-binary @bulk_answers.txt
curl -XPOST "http://estest.michaelblouin.com:9182/_bulk?pretty" --data-binary @bulk_comments.txt
