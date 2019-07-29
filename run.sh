sh stop.sh

echo 'start cinema-bot'
nohup java -jar target/cinema-bot-0.0.1-SNAPSHOT.jar --bot.telegram.token=$CINEMA_BOT_TOKEN > /dev/null &

echo 'END run.sh'
