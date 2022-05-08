import { Component, OnInit } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AdamosHubService } from '../shared/adamosHub.service';
import { ITreeNode } from '../shared/model/ITreeNode';

@Component({
  selector: 'hierarchy-tree',
  templateUrl: './hierarchy-tree.component.html'
})
export class HierarchyTreeComponent implements OnInit {

  public title : String = "Hierarchy";
  isLoading$: BehaviorSubject<boolean> = new BehaviorSubject(false);
  public tree: Array<ITreeNode<any>>;

  constructor(private hubService: AdamosHubService) {
    this.loadTree();
  }

  ngOnInit() {
  }

  async loadTree() {
    this.isLoading$.next(true);

    this.hubService.getTree$().subscribe(data => {
      this.tree = data;
      this.isLoading$.next(false);
    });
  }

}
