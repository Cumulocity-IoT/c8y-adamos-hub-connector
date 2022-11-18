import { Component, OnChanges, Input, Output, EventEmitter } from '@angular/core';

export interface Segment {
  parent: string;
  key: string;
  value: any;
  type: undefined | string;
  description: string;
  expanded: boolean;
}

@Component({
  selector: 'ngx-json-viewer',
  templateUrl: './ngx-json-viewer.component.html'//,
  //styleUrls: ['./ngx-json-viewer.component.css']

})
export class NgxJsonViewerComponent implements OnChanges {

  @Input() json: any;
  @Input() expanded = true;
  /**
   * @deprecated It will be always true and deleted in version 3.0.0
   */
  @Input() cleanOnChange = true;
  @Input() parent: string;
  @Output() valueOnChange: EventEmitter<any> = new EventEmitter();

  segments: Segment[] = [];

  ngOnChanges() {
    if (this.cleanOnChange) {
      this.segments = [];
    }

    if (typeof this.json === 'object') {
      Object.keys(this.json).forEach( key => {
        this.segments.push(this.parseKeyValue(this.parent, key, this.json[key]));
      });
    } else {
      this.segments.push(this.parseKeyValue(this.parent, `(${typeof this.json})`, this.json));
    }
  }

  isExpandable(segment: Segment) {
    return segment.type === 'object' || segment.type === 'array';
  }

  toggle(segment: Segment) {
    if (this.isExpandable(segment)) {
      segment.expanded = !segment.expanded;
    }
  }

  changeValue(value: String, segment: Segment) {
    segment.value = value;
    this.valueOnChange.emit(segment);
  }

  private parseKeyValue(parent: any, key: any, value: any): Segment {
    const segment: Segment = {
      parent: parent,
      key: key,
      value: value,
      type: undefined,
      description: '' + value,
      expanded: this.expanded
    };

    switch (typeof segment.value) {
      case 'number': {
        segment.type = 'number';
        break;
      }
      case 'boolean': {
        segment.type = 'boolean';
        break;
      }
      case 'function': {
        segment.type = 'function';
        break;
      }
      case 'string': {
        segment.type = 'string';
        segment.description = '"' + segment.value + '"';
        break;
      }
      case 'undefined': {
        segment.type = 'undefined';
        segment.description = 'undefined';
        break;
      }
      case 'object': {
        // yea, null is object
        if (segment.value === null) {
          segment.type = 'null';
          segment.description = 'null';
        } else if (Array.isArray(segment.value)) {
          segment.type = 'array';
          segment.description = 'Array[' + segment.value.length + '] ' + JSON.stringify(segment.value);
        } else if (segment.value instanceof Date) {
          segment.type = 'date';
        } else {
          segment.type = 'object';
          segment.description = 'Object ' + JSON.stringify(segment.value);
        }
        break;
      }
    }

    return segment;
  }
}
