import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricsLanguage, tr} from "./sakai-rubrics-language.js";
import {SakaiRubricsHelpers} from "./sakai-rubrics-helpers.js";

class SakaiRubricStudentButton extends RubricsElement {

  constructor() {

    super();

    this.hidden = true;
    this.instructor = false;
    this.rubricsUtils.initLightbox(true);
    SakaiRubricsLanguage.loadTranslations().then(result => this.i18nLoaded = result);
  }

  static get properties() {

    return {
      entityId: { attribute: "entity-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      evaluatedItemId: { attribute: "evaluated-item-id", type: String},
      hidden: { type: Boolean },
      instructor: { type: Boolean },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.toolId && this.entityId) {
      this.setHidden();
    }
  }

  render() {

    return html`${this.hidden ? html``
      : html`
      <span class="fa fa-table" style="cursor: pointer;" title="${tr("preview_rubric")}" @click="${this.showRubric}" />
    `}`;
  }

  showRubric() {

    this.rubricsUtils.showRubric(undefined, {"tool-id": this.toolId, "entity-id": this.entityId, "evaluated-item-id": this.evaluatedItemId, "instructor": this.instructor});
  }

  setHidden() {

    SakaiRubricsHelpers.get("/rubrics-service/rest/rubric-associations/search/by-tool-item-ids", { params: {toolId: this.toolId, itemId: this.entityId }})
    .then(data => {

      const association = data._embedded["rubric-associations"][0];

      if (!association) {
        this.hidden = true;
      } else {
        this.hidden = association.parameters.hideStudentPreview && !this.instructor;
      }
    });
  }
}

customElements.define("sakai-rubric-student-button", SakaiRubricStudentButton);
