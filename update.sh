sh ~/cinema-bot/stop.sh

git -C ~/cinema-bot config user.email "kang3q@gmail.com"
git -C ~/cinema-bot config user.name "kang3q"

echo 'git pull'
git -C ~/cinema-bot fetch --all
git -C ~/cinema-bot reset --hard origin/master
git -C ~/cinema-bot pull origin master

echo 'mvn clean package'
mvn clean package -Dmaven.test.skip=true -pl ~/cinema-bot -am

sh ~/cinema-bot/run.sh

echo 'END update.sh'
