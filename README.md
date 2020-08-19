# Janssen demo app

The demo app for the Janssen project. With two biomarkers: 
- Walking speed test
- Congintion test

### Install

Requirements:
- Node
- Cordova

```
git clone https://github.com/orikami-nl/janssen-demo-app.git
npm install
```

## Development

**Run app local on port 8080**
```
npm run start
```

**Update code cordova**
Update the following files with the correct **CodePushDeploymentKey**. You can find the key in the dashboard of AppCenter under: Distribute -> CodePush -> right corner settings icon. Do this for both iOS and Android:
- cordova/config.xml
```
npm run cordova
cd cordova
cordova prepare
npm run release
```
