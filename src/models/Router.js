import { observable, action } from "mobx";

class Router {
  @observable page = "/";
  @observable params = null;

  @action.bound
  go(page, params) {
    this.params = params;
    this.page = page;
  }
}
export default Router;