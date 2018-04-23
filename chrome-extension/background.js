// 공식문서 https://developer.chrome.com/extensions/overview
// 샘플     https://developer.chrome.com/extensions/samples#search:
// api 문서 https://developer.chrome.com/extensions/api_index
// 참고 https://minwook-shin.github.io/chrome-extension/

function noti(text) {
  chrome.notifications.clear('cinema-noti');
  chrome.notifications.create('cinema-noti', {
    type: 'basic',
    title: '영화 1+1',
    message: text || '영화 티켓 나왓다.',
    iconUrl: 'icon.png',
  }/*, () => {}*/);
  //chrome.browserAction.setBadgeText({ text: '2' });
}

//setTimeout(noti, 300);

chrome.notifications.onClicked.addListener(() => {
  chrome.notifications.clear('cinema-noti');
  //noti();
});

let stompClient;

function connect() {
  const socket = new SockJS('http://localhost:8081/websocket-1p1');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function(frame) {
    //console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/greetings', function(greeting) {
      noti(JSON.parse(greeting.body).content);
    });
  });

  //setTimeout(() => stompClient.send('/app/hello', {}, JSON.stringify({ 'name': '강상규!!!' })), 1000);
}

connect();