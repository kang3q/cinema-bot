sudo pkill -9 -ef cinema-bot
sudo nohup java -jar /home/pi/cinema-bot/target/cinema-bot-0.0.1-SNAPSHOT.jar --bot.telegram.token=$CINEMA_BOT_TOKEN &

echo 'END run-auto.sh'
