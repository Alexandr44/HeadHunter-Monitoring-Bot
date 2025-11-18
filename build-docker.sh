./gradlew clean bootJar
docker build -t headhuntermonitoringbot:1.0 .
docker save headhuntermonitoringbot:1.0 -o headhuntermonitoringbot.tar
scp ./headhuntermonitoringbot.tar mediaserver@192.168.0.200:/tmp/
