import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'convertCamelCaseToSpace'
})
export class ConvertCamelCaseToSpace implements PipeTransform {

  transform(value: string): string {
        if (typeof value !== "string") {
          return value;
        }
    
        return value.split(/(?=[A-Z])/).join(' ');
  }
}