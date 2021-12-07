import { observable, action } from 'mobx'
import querystring from "qs";

class Router {
  @observable page = "/";
  @observable params = null;

  @action.bound
  go(page, params) {
    if (page && page.split('?').length > 1) {
      params = querystring.parse(page.split("?")[1] || "")
      page = page.match(/\/?([^?]*)/)[0]
    }
    this.page = page
    this.params = params
  }
}
export default Router