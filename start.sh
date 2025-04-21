# run.sh
#!/bin/bash
./gradlew buildAllImages --parallel && docker-compose up
