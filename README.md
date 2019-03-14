my-rn-game-service: Leadboard
## Installation

##### Thêm Vào package.json
```
"my-rn-game-service": "git+https://gitlab.com/react-native-my-libs/my-rn-game-service.git",
```

Chạy  lệnh sau
```
npm install
```

### Android
```javascript

include ':my-rn-game-service'
project(':my-rn-game-service').projectDir = new File('S:/Codes/react-native-my-libs/my-rn-game-service/android')
include ':BaseGameUtils'
project(':BaseGameUtils').projectDir = new File('S:/Codes/react-native-my-libs/my-rn-game-service/BaseGameUtils')


new RNGameServicePackage(),
```
