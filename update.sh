pkill -9 -ef cinema-bot
git pull
mvn -Dmaven.test.skip=true package
nohup java -jar target/cinema-bot-0.0.1-SNAPSHOT.jar --bot.telegram.token=$CINEMA_BOT_TOKEN &
