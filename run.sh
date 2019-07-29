sh stop.sh

echo 'start cinema-bot'
nohup java -jar target/cinema-bot-0.0.1-SNAPSHOT.jar --bot.telegram.token=$CINEMA_BOT_TOKEN 1> /dev/null 2>&1 &

echo 'END run.sh'
