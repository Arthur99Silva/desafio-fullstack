import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, catchError, map } from 'rxjs';
import { CepInfo } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CepService {
  constructor(private http: HttpClient) {}

  consultar(cep: string): Observable<CepInfo> {
    const cepLimpo = cep.replace(/\D/g, '');

    if (cepLimpo.length !== 8) {
      return of({
        cep: cepLimpo,
        uf: '',
        cidade: '',
        bairro: '',
        logradouro: '',
        valido: false,
        mensagem: 'CEP deve conter 8 dígitos',
      });
    }

    return this.http
      .get<CepInfo>(`${environment.apiUrl}/cep/${cepLimpo}`)
      .pipe(
        catchError(() => this.consultarViaCepDireto(cepLimpo))
      );
  }

  // consulta no Viacep
  private consultarViaCepDireto(cep: string): Observable<CepInfo> {
    return this.http
      .get<any>(`https://viacep.com.br/ws/${cep}/json/`)
      .pipe(
        map((data) => {
          if (data.erro) {
            return {
              cep,
              uf: '',
              cidade: '',
              bairro: '',
              logradouro: '',
              valido: false,
              mensagem: 'CEP não encontrado',
            };
          }
          return {
            cep,
            uf: data.uf || '',
            cidade: data.localidade || '',
            bairro: data.bairro || '',
            logradouro: data.logradouro || '',
            valido: true,
          };
        }),
        catchError(() =>
          of({
            cep,
            uf: '',
            cidade: '',
            bairro: '',
            logradouro: '',
            valido: false,
            mensagem: 'Erro ao consultar CEP',
          })
        )
      );
  }
}
