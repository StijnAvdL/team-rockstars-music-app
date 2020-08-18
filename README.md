# Janssen demo app

### Install

Requirements:
- Node
- Cordova

```
git clone https://github.com/orikami-nl/crick-receiver-modules.git
npm install
```

**Initialize submodules in an existing repository**
```
git submodule init
git submodule update
cd orikami
git checkout linechart
```

## Development

**Run app local on port 8080**
```
npm run start
```

**Update code Android & iOS staging**
Update the following files with the correct **CodePushDeploymentKey**. You can find the key in the dashboard of AppCenter under: Distribute -> CodePush -> right corner settings icon. Do this for both iOS and Android:
- cordova/config.xml
- cordova/platform/android/app/src/main/res/config.xml
- cordova/platform/ios/MS sherpa/config.xml
```
npm run cordova
cd cordova
npm run release
```

## Cordova development
Add platforms
```
cordova platform add android@7.1.4
cordova platform add ios@5.0.1
```

**Install extra plugin**
```
cordova prepare
ok cordova install uploader
ok cordova install cordova-orikami-helper-plugin
ok cordova install visual-response-test vrt-plugin
ok cordova install sensingkit sensingkit-cordova-plugin
```

**Build app on iOS**
Open xCode and run app

**Run app on Android**
```
cd cordova
cordova run android
```

## Build for production
First get the .env file from dashlane named: *MS sherpa production .env* place in it in the root with the name *.env-production*
```
npm run cordova-production
cd cordova
npm run release-production
```

Update the following files with the correct **CodePushDeploymentKey**. You can find the key in the dashboard of AppCenter under: Distribute -> CodePush -> right corner settings icon. Do this for both iOS and Android:
- cordova/config.xml
- cordova/platform/android/app/src/main/res/config.xml
- cordova/platform/ios/MS sherpa/config.xml

Now you are ready to build the production app for both iOS and Android. 

## Software Architecture

The application is organized around components: standalone user-interface elements.
We use *React* for rendering and *Mobx* for state management.

### Layers
The application is best understood as a collection of layers. From dumb to smart:

1. **Components** 
  - Consists of two (!) React Components:
  - 1) Template Component: Renders HTML, no interaction. Ideally, a stateless and pure function.
  - 2) Controller Component: Implements user-interface logic.
  - EXCLUDED: Models, Services, third-party libraries, server-calls, etc.
  - INCLUDED: Other components.
  - Component API: properties and callbacks (i.e. to "save" or "cancel")

2. **ViewModel**
  - Connect the View (a Component) to the rest of the world (Models & Services)
  - Example: Adding server-calls to retrieve and save a note to the Note component.
  - EXCLUDED: reusable business logic, such as dealing with a User, Authentication, etc.
  - INCLUDED: ViewModels use Components and depend on Services and Models.
  - ViewModel API: None - often directly imports the models

3. **Models**
  - Contains business logic, often related to database entities (i.e. an User, Experiment, etc).
  - INCLUDED: A model can depend on other models and services.
  - INCLUDED: Business logic.
  - EXCLUDED: The browser api (or other low-level javascript and dependencies).
  - EXCLUDED: A model does not use Components or ViewModels.
  - Model API: None - often directly imports other models and services.

4. **Services**
  - A service is like a library: it abstracts low-level javascript in a simple, reusable wrapper.
  - Example: fetching data from a server, dealing with localstorage, calculations or algorithms, event-emitters, etc.
  - INCLUDED: Third-party libraries, browser api.
  - EXCLUDED: Components, ViewModels and Models.
  - Service API: Whatever makes sense.
