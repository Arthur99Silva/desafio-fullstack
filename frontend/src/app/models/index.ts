export interface Empresa {
  id?: number;
  cnpj: string;
  nomeFantasia: string;
  cep: string;
  logradouro?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
  criadoEm?: string;
  atualizadoEm?: string;
  fornecedores?: FornecedorSimple[];
}

export interface EmpresaRequest {
  cnpj: string;
  nomeFantasia: string;
  cep: string;
}

export interface EmpresaSimple {
  id: number;
  cnpj: string;
  nomeFantasia: string;
  cep: string;
  cidade?: string;
  uf?: string;
}

export interface Fornecedor {
  id?: number;
  cpfCnpj: string;
  tipoPessoa: TipoPessoa;
  nome: string;
  email: string;
  cep: string;
  rg?: string;
  dataNascimento?: string;
  logradouro?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
  criadoEm?: string;
  atualizadoEm?: string;
  empresas?: EmpresaSimple[];
}

export interface FornecedorRequest {
  cpfCnpj: string;
  tipoPessoa: TipoPessoa;
  nome: string;
  email: string;
  cep: string;
  rg?: string;
  dataNascimento?: string;
}

export interface FornecedorSimple {
  id: number;
  cpfCnpj: string;
  tipoPessoa: TipoPessoa;
  nome: string;
  email: string;
}

export type TipoPessoa = 'FISICA' | 'JURIDICA';

export interface CepInfo {
  cep: string;
  uf: string;
  cidade: string;
  bairro: string;
  logradouro: string;
  valido: boolean;
  mensagem?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
  errors?: Record<string, string>;
}
