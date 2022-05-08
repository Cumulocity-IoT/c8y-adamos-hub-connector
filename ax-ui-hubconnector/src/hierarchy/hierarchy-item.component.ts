import { Component, OnInit, Input, ViewChildren } from '@angular/core';
import { ITreeNode } from '../shared/model/ITreeNode';
import { AdamosHubService } from '../shared/adamosHub.service';

@Component({
  selector: 'hierarchy-item',
  templateUrl: './hierarchy-item.component.html'
})
export class HierarchyItemComponent implements OnInit {
  @Input()
  private item: ITreeNode<any>;

  @Input()
  private isHidden: boolean = true;
  private isDown: boolean = true;

  @ViewChildren('childItems') public fieldsList;

  constructor(private hubService: AdamosHubService) { }

  ngOnInit() {
  }

 
  // public dragStart(event: DragEvent, item: ITreeNode<any>){
  //   if (item.level == 4) {
  //     this.hubService.setDragItem(item);
  //     console.log(item);
  //   }
  // }

  // public dragEnd(event: DragEvent, item: ITreeNode<any>){
  //   this.hubService.setDragItem(undefined);
  // }
  
  // public dragOver(event: DragEvent, dropArea: ITreeNode<any>){
  //   if (dropArea.level == 3) {
  //     event.preventDefault();
  //   }
  // }

  // public drop(event: DragEvent, dropArea: ITreeNode<any>){
  //   // const index = this.hubService.getDragSource().fieldsList.indexOf(this.hubService.getDragItem());
  //   // this.hubService.getDragSource().fieldsList.splice(index, 1);    
  //   dropArea.children.push(this.hubService.getDragItem());
  //   this.hubService.setDragItem(undefined);
  // }


  toggle() {
    this.fieldsList.forEach((child: HierarchyItemComponent) => { child.isHidden = !child.isHidden });
    this.isDown = !this.isDown;
  }

  name() {
    if (this.item.data.name != null) {
      return this.item.data.name;
    } else {
      return this.item.data.customerIdentification.name;
    }
  }

}
