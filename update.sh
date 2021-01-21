sh stop.sh

git config user.email "kang3q@gmail.com"
git config user.name "kang3q"

echo 'git pull'
git fetch --all
git reset --hard origin/master
git pull origin master

echo 'mvn clean package'
mvn -Dmaven.test.skip=true clean package

sh run.sh

echo 'END update.sh'
