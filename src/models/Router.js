import { observable, action, reaction } from "mobx";

class Router {
  @observable page = "/";
  @observable params = null;

  constructor() {
    // set listener to hard back button Android
    // document.addEventListener('backbutton', this.goBack);
  }

  @action.bound
  go(page, params) {
    this.params = params;
    this.page = page;
  }

//   @action.bound
//   goBack() {
//     if(this.page === "/questions") {
//       this.go("/timeline");
//     } else {
//       navigator.app.exitApp();
//     }
//   }
}
export default Router;