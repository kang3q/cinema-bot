sh ~/cinema-bot/stop.sh

echo 'start cinema-bot'
nohup java -jar ~/cinema-bot/target/cinema-bot-0.0.1-SNAPSHOT.jar --spring.profiles.active=production --spring.bot.telegram.token=$CINEMA_BOT_TOKEN &

echo 'END run.sh'
