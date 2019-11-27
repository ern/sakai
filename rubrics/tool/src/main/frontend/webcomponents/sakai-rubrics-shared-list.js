import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {repeat} from "/webcomponents/assets/lit-html/directives/repeat.js";
import {SakaiRubricReadonly} from "./sakai-rubric-readonly.js";
import {SakaiRubricsHelpers} from "./sakai-rubrics-helpers.js";

export class SakaiRubricsSharedList extends RubricsElement {

  constructor() {

    super();

    this.getSharedRubrics();
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
      <div role="tablist">
      ${repeat(this.rubrics, r => r.id, r => html`
        <div class="rubric-item" id="rubric_item_${r.id}">
          <sakai-rubric-readonly rubric="${JSON.stringify(r)}" @copy-to-site="${this.copyToSite}"></sakai-rubric-readonly>
        </div>
      `)}
      </div>
    `;
  }

  refresh() {
    this.getSharedRubrics();
  }

  getSharedRubrics() {

    var params = {"projection": "inlineRubric"};

    SakaiRubricsHelpers.get("/rubrics-service/rest/rubrics/search/shared-only", { params })
      .then(data => this.rubrics = data._embedded.rubrics );
  }

  copyToSite(e) {

    var options = { extraHeaders: { "x-copy-source": e.detail, "lang": portal.locale  } };
    SakaiRubricsHelpers.post("/rubrics-service/rest/rubrics/", options)
      .then(data => this.dispatchEvent(new CustomEvent("copy-share-site")));
  }
}

customElements.define("sakai-rubrics-shared-list", SakaiRubricsSharedList);
