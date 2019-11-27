import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricCriterionPreview} from "./sakai-rubric-criterion-preview.js";

export class SakaiRubricPreview extends RubricsElement {

  constructor() {

    super();

    this.rubricId = 0;
    this.rubric = {};
    this.gradeFieldId = 0;
  }

  static get properties() {

    return {
      rubric: {type: Object},
      rubricId: { attribute: "rubric-id", type: Number },
      gradeFieldId: String,
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ("rubric-id" == name) {
      this.idChanged();
    }
  }

  shouldUpdate(changedProperties) {
    return changedProperties.has("rubric") && this.rubric.criterions;
  }

  render() {

    return html`
      <h3>${this.rubric.title}</h3>
      <p>${this.rubric.description}</p>

      <sakai-rubric-criterion-preview
        criteria="${JSON.stringify(this.rubric.criterions)}"
        gradeFieldId="${this.gradeFieldId}"
        ></sakai-rubric-criterion-preview>
    `;
  }

  idChanged() {

    setTimeout(function () {

      $.ajax({
        url: `/rubrics-service/rest/rubrics/${this.rubricId}?projection=inlineRubric`,
        contentType: "application/json"
      })
      .done(data => this.rubric = data)
      .fail((jqXHR, textStatus, errorThrown) => { console.log(textStatus); console.log(errorThrown); });
    }.bind(this));
  }
}

customElements.define("sakai-rubric-preview", SakaiRubricPreview);
