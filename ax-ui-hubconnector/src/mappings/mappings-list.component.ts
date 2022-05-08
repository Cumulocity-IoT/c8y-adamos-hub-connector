import { _ } from '@c8y/ngx-components';
import { Component } from '@angular/core';
import { IManagedObject } from '@c8y/client';
import { AdamosHubService } from '../shared/adamosHub.service';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, combineLatest } from 'rxjs';
import { IHubResponse } from '../shared/model/IHubResponse';

/* 
 * Lists all devices of the current tenant with the posibility to add or remove Hub-Settings. 
 */
@Component({
    selector: 'mappings',
    templateUrl: './mappings-list.component.html'
})
export class MappingsListComponent {
    mappings: Array<IHubResponse<any>>;
    informationText: string;
    defaultMappingId: string;
    isLoading$: BehaviorSubject<boolean> = new BehaviorSubject(false);

    constructor(private router: Router, private hubService: AdamosHubService) {
        // _ annotation to mark this string as translatable string.
        this.informationText = _('Ooops! It seems that there is no device to display.');
        this.loadMappings();
      }

    onClickDelete(id: number) {
        this.hubService.deleteMapping(id).subscribe(item => this.loadMappings());
    }

    onClickDuplicate(id: number) {
        this.hubService.duplicateMapping$(id).subscribe(item => this.loadMappings());
    }

    onClickAdd() {
        this.hubService.addMapping$().subscribe((item) => this.router.navigate(["mappings/", item.id]));
    }

    async loadMappings() {
        this.isLoading$.next(true);

        combineLatest(
            this.hubService.getMappings$() ,
            this.hubService.getGlobalSettings$(),
            (mappings: Array<IHubResponse<any>>, globalSettings: any) => ({mappings, globalSettings})
        ).subscribe(data => {
            this.mappings = data.mappings;
            this.defaultMappingId = data.globalSettings.defaultMappingId;
            this.isLoading$.next(false);
        });
    }

    onRefresh() {
        this.loadMappings();
    }
}
