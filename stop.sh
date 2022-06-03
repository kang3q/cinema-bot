echo 'pkill cinema-bot'
#pkill -9 -ef cinema-bot
kill -9 `cat application.pid`

echo 'END stop.sh'
