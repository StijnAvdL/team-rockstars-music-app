import { observable, action } from "mobx";

class Menu {
    @observable open = false;

    @action.bound
    close() {
        this.open = false;
    }

    @action.bound
    toggle() {
        this.open = !this.open;
    }
}
export default new Menu();