import { _ } from '@c8y/ngx-components';
import { Component } from '@angular/core';
import { IManagedObject } from '@c8y/client';
import { TranslateService } from '@ngx-translate/core';
import { AdamosHubService } from '../shared/adamosHub.service';
import { BehaviorSubject } from 'rxjs';
import { IHubResponse } from '../shared/model/IHubResponse';

/* 
 * Dialog to setup tenant-wide settings for HubConnector
 */
@Component({
    selector: 'settings',
    templateUrl: './settings.component.html'
})
export class SettingsComponent {
    globalSettings: IManagedObject;
    informationText: string;
    currentLang: string;
    mappings: any;
    selectedMapping: any;
    isLoading$: BehaviorSubject<boolean> = new BehaviorSubject(false);
    
    constructor(translateService: TranslateService, private hubService: AdamosHubService) {
        // _ annotation to mark this string as translatable string.
        this.informationText = _('Ooops! It seems that there are no settings to display.');
        this.currentLang = translateService.currentLang;

        this.loadData();
    }

    async loadData() {
        this.isLoading$.next(true);

        await this.hubService.getMappings().then((value) => {
            this.mappings = value;
        });

        this.hubService.getGlobalSettings$().subscribe(data => {
            this.globalSettings = data;
            if(this.globalSettings.amqpCredentials==null) {
                this.globalSettings.amqpCredentials={};
            }
            this.selectedMapping = this.mappings.find((x: any) => x.id == this.globalSettings.defaultMappingId);
            //this.dataLoaded = Promise.resolve(true)
            this.isLoading$.next(false);
        })
    }

    onMappingClick(mapping: IHubResponse<any>) {
        this.selectedMapping = mapping;
        this.globalSettings.defaultMappingId = mapping.id;
    }

    // Save the current settings to the inventory
    async saveSettings() {
        this.isLoading$.next(true);
        if (this.globalSettings != undefined) {
            this.hubService.updateGlobalSettings$(this.globalSettings).subscribe(data => {
                this.globalSettings = data;
                this.isLoading$.next(false);
            })
        }
    }
}
