import { Injectable } from "@angular/core";
import { FetchClient, IManagedObject } from "@c8y/client";
import { isArray } from "lodash-es";
import { AdamosHubDevice } from "./model/AdamosDevice";

@Injectable({ providedIn: "root" })
export class NewAdamosHubService {
  private hubUrl = "/service/hubconnector";
  private headers: object = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };

  constructor(private fc: FetchClient) {}

  async getEquipment(params?: object): Promise<AdamosHubDevice[]> {
    const url = `${this.hubUrl}/assets`;

    const response = await this.fc.fetch(url, {
      method: "GET",
      headers: this.headers,
      params,
    });

    if (response.status !== 200) {
      const error = new Error();
      error.message = `Status not ok (${response.status})`;
      throw error;
    }

    const data = await response.json();
    if (isArray(data)) {
      return data as AdamosHubDevice[];
    } else {
      return [];
    }
  }

  async getDisconnectedEquipment(): Promise<AdamosHubDevice[]> {
    return this.getEquipment({ disconnected: true });
  }

  async linkHubDevice(uuid: string, isDevice: boolean = true): Promise<any> {
    const url = `${this.hubUrl}/synchronization/fromHub/${uuid}?isDevice=${isDevice}`;
    const response = await this.fc.fetch(url, {
      method: "POST",
      headers: this.headers,
    });

    if (response.status !== 201) {
      const error = new Error();
      error.message = `Status not ok (${response.status})`;
      throw error;
    }

    const data = await response.json();
    return data;
  }

  async linkCumulocityDevice(id: string): Promise<IManagedObject> {
    const url = `${this.hubUrl}/synchronization/toHub/${id}`;
    const response = await this.fc.fetch(url, {
      method: "POST",
      headers: this.headers,
    });

    if (response.status !== 201) {
      const error = new Error();
      error.message = `Status not ok (${response.status})`;
      throw error;
    }

    const data = (await response.json()) as IManagedObject;
    return data;
  }

  async unlink(id: string) {
    const url = `${this.hubUrl}/synchronization/unlink/${id}`;
    const response = await this.fc.fetch(url, {
      method: "POST",
      headers: this.headers,
    });

    if (response.status !== 204) {
      const error = new Error();
      error.message = `Status not ok (${response.status})`;
      throw error;
    }
  }

  async linkExisting(uuid: string, moId: string) {
    const url = `${this.hubUrl}/synchronization/sync/${uuid}/with/${moId}`;
    const response = await this.fc.fetch(url, {
      method: "POST",
      headers: this.headers,
    });

    if (response.status !== 200) {
      const error = new Error();
      error.message = `Status not ok (${response.status})`;
      throw error;
    }

    const data = await response.json();
    return data;
  }
}
