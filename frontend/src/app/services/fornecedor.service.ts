import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Fornecedor, FornecedorRequest, PageResponse } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private readonly url = `${environment.apiUrl}/fornecedores`;

  constructor(private http: HttpClient) {}

  findAll(nome = '', cpfCnpj = '', page = 0, size = 10): Observable<PageResponse<Fornecedor>> {
    const params = new HttpParams()
      .set('nome', nome)
      .set('cpfCnpj', cpfCnpj)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<Fornecedor>>(this.url, { params });
  }

  findById(id: number): Observable<Fornecedor> {
    return this.http.get<Fornecedor>(`${this.url}/${id}`);
  }

  create(fornecedor: FornecedorRequest): Observable<Fornecedor> {
    return this.http.post<Fornecedor>(this.url, fornecedor);
  }

  update(id: number, fornecedor: FornecedorRequest): Observable<Fornecedor> {
    return this.http.put<Fornecedor>(`${this.url}/${id}`, fornecedor);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
