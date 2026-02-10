import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Empresa, EmpresaRequest, PageResponse } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EmpresaService {
  private readonly url = `${environment.apiUrl}/empresas`;

  constructor(private http: HttpClient) {}

  findAll(search = '', page = 0, size = 10): Observable<PageResponse<Empresa>> {
    const params = new HttpParams()
      .set('search', search)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<Empresa>>(this.url, { params });
  }

  findById(id: number): Observable<Empresa> {
    return this.http.get<Empresa>(`${this.url}/${id}`);
  }

  create(empresa: EmpresaRequest): Observable<Empresa> {
    return this.http.post<Empresa>(this.url, empresa);
  }

  update(id: number, empresa: EmpresaRequest): Observable<Empresa> {
    return this.http.put<Empresa>(`${this.url}/${id}`, empresa);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  vincularFornecedor(empresaId: number, fornecedorId: number): Observable<Empresa> {
    return this.http.post<Empresa>(`${this.url}/${empresaId}/fornecedores/${fornecedorId}`, {});
  }

  desvincularFornecedor(empresaId: number, fornecedorId: number): Observable<Empresa> {
    return this.http.delete<Empresa>(`${this.url}/${empresaId}/fornecedores/${fornecedorId}`);
  }
}
