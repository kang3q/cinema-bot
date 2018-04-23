echo 'pkill cinema-bot'
pkill -9 -ef cinema-bot

echo 'start cinema-bot'
nohup java -jar target/cinema-bot-0.0.1-SNAPSHOT.jar --bot.telegram.token=$CINEMA_BOT_TOKEN &

echo 'END run.sh'