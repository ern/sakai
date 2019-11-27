import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {repeat} from "/webcomponents/assets/lit-html/directives/repeat.js";
import {SakaiRubric} from "./sakai-rubric.js";
import {SharingChangeEvent} from "./sharing-change-event.js";
import {SakaiRubricsHelpers} from "./sakai-rubrics-helpers.js";

export class SakaiRubricsList extends RubricsElement {

  constructor() {

    super();
    this.getRubrics();
  }

  static get properties() {

    return {
      rubrics: { type: Array },
    };
  }

  shouldUpdate(changedProperties) {
    return changedProperties.has("rubrics");
  }

  render() {

    return html`
      <div role="presentation">
        <div role="tablist">
        ${repeat(this.rubrics, r => r.id, r => html`
          <div class="rubric-item" id="rubric_item_${r.id}">
            <sakai-rubric @clone-rubric="${this.cloneRubric}" @delete-item="${this.deleteRubric}" rubric="${JSON.stringify(r)}"></sakai-rubric>
          </div>
        `)}
        </div>
      </div>
      <br>
      <div class="act">
        <button class="active add-rubric" @click="${this.createNewRubric}">
            <span class="add fa fa-plus"></span>
            <sr-lang key="add_rubric">add_rubric</sr-lang>
        </button>
      </div>
    `;
  }

  refresh() {
    this.getRubrics();
  }

  getRubrics(extraParams = {}) {

    var params = {"projection": "inlineRubric"};
    Object.assign(params, extraParams);

    SakaiRubricsHelpers.get("/rubrics-service/rest/rubrics", { params })
      .then(data => {

        this.rubrics = data._embedded.rubrics;

        if (data.page.size <= this.rubrics.length){
          this.getRubrics({ "size": this.rubrics.length + 25 });
        }
      });
  }

  createRubricResponse(nr) {

    nr.new = true;

    // Make sure criterions are set, otherwise lit-html borks in sakai-rubric-criterion.js
    if (!nr.criterions) {
      nr.criterions = [];
    }

    this.rubrics.push(nr);

    var tmp = this.rubrics;
    this.rubrics = [];
    this.rubrics = tmp;

    this.requestUpdate();
    this.updateComplete.then(async() => {
      await this.createRubricUpdateComplete;
      this.querySelector(`#rubric_item_${nr.id} sakai-rubric`).toggleRubric();
    });
  }

  deleteRubric(e) {

    e.stopPropagation();
    this.rubrics.splice(this.rubrics.map(r => r.id).indexOf(e.detail.id), 1);

    var tmp = this.rubrics;
    this.rubrics = [];
    this.rubrics = tmp;

    this.dispatchEvent(new SharingChangeEvent());

    this.requestUpdate();
  }

  cloneRubric(e) {

    SakaiRubricsHelpers.post("/rubrics-service/rest/rubrics/", {
      extraHeaders: {"x-copy-source": e.detail.id, "lang": portal.locale}
    })
    .then(data => this.createRubricResponse(data));
  }

  createNewRubric() {

    SakaiRubricsHelpers.post("/rubrics-service/rest/rubrics/", {
      extraHeaders: {"x-copy-source" :"default", "lang": portal.locale}
    })
    .then(data => this.createRubricResponse(data));
  }

  get createRubricUpdateComplete() {
    return (async () => {
      return await this.querySelector(`#rubric_item_${this.rubrics[this.rubrics.length - 1].id} sakai-rubric`).updateComplete;
    })();
  }
}

customElements.define("sakai-rubrics-list", SakaiRubricsList);
