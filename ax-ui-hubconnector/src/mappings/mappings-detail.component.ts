import { _ } from '@c8y/ngx-components';
import { Component } from '@angular/core';
import { InventoryService, IManagedObject, UserService } from '@c8y/client';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { AdamosHubService } from '../shared/adamosHub.service';
import { IManufacturerIdentity } from 'src/shared/model/IManufacturerIdentity';

/*
 * Loads the selected device to configure the HubSettings in detail.
 */
@Component({
    selector: 'mapping',
    templateUrl: './mappings-detail.component.html'
})
export class MappingsDetailComponent {
    mapping: any;
    manufacturerIdentitiesList: Array<IManufacturerIdentity>;
    informationText: string;
    isLoading$: BehaviorSubject<boolean> = new BehaviorSubject(false);
    
    constructor(private inventory: InventoryService,
                private route: ActivatedRoute, 
                private router: Router,
                private hubService: AdamosHubService) {
        // _ annotation to mark this string as translatable string.
        this.informationText = _('Ooops! It seems that there is no mapping to display.');
        this.loadMapping();
    }

    // Promise-based usage of InventoryService.
    async loadMapping() {
        this.isLoading$.next(true);

        const id = parseInt(this.route.snapshot.paramMap.get('id'));
        // const { data, res } = await this.inventory.detail(id);
        // this.mapping = data;

        combineLatest(
            this.hubService.getMapping$(id),
            this.hubService.getManufacturerIdentities$(),
            (mapping: any, manufacturerIdentities: Array<IManufacturerIdentity>) => ({ mapping, manufacturerIdentities })
        ).subscribe(data => {
            this.mapping = data.mapping;
            this.manufacturerIdentitiesList = data.manufacturerIdentities;
            this.isLoading$.next(false);
        });
    }

    // Update the current mapping with the new settings
    async saveMapping() {
        if (this.mapping != undefined) {
            this.hubService.updateMapping$(this.mapping).subscribe(data => {
                this.router.navigate(["/mappings"]);
            });
        }
    }

    onModelChanged(event: any) {
        if (event.parent == undefined) {
            this.mapping.data.model[event.key] = event.value;
        } else {
            this.mapping.data.model[event.parent][event.key] = event.value;
        }
    }

}