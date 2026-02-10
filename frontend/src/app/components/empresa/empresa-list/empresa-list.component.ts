import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EmpresaService } from '../../../services/empresa.service';
import { FornecedorService } from '../../../services/fornecedor.service';
import { NotificationService } from '../../../services/notification.service';
import { Empresa, Fornecedor, PageResponse } from '../../../models';
import { CpfCnpjPipe } from '../../../pipes/cpf-cnpj.pipe';
import { CepFormatPipe } from '../../../pipes/cep-format.pipe';

@Component({
  selector: 'app-empresa-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, CpfCnpjPipe, CepFormatPipe],
  templateUrl: './empresa-list.component.html',
})
export class EmpresaListComponent implements OnInit {
  page: PageResponse<Empresa> | null = null;
  search = '';
  loading = false;
  searchTimeout: any;

  empresaToDelete: Empresa | null = null;
  empresaVincular: Empresa | null = null;
  fornecedoresDisponiveis: Fornecedor[] = [];
  fornecedorIdVincular: number | null = null;

  Math = Math;

  constructor(
    private empresaService: EmpresaService,
    private fornecedorService: FornecedorService,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadEmpresas();
  }

  loadEmpresas(page = 0): void {
    this.loading = true;
    this.empresaService.findAll(this.search, page).subscribe({
      next: (data) => {
        this.page = data;
        this.loading = false;
      },
      error: (err) => {
        this.notification.error('Erro ao carregar empresas');
        this.loading = false;
      },
    });
  }

  onSearch(): void {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => this.loadEmpresas(), 400);
  }

  goToPage(page: number): void {
    this.loadEmpresas(page);
  }

  confirmDelete(empresa: Empresa): void {
    this.empresaToDelete = empresa;
  }

  deleteEmpresa(): void {
    if (!this.empresaToDelete?.id) return;
    this.empresaService.delete(this.empresaToDelete.id).subscribe({
      next: () => {
        this.notification.success('Empresa excluída com sucesso');
        this.empresaToDelete = null;
        this.loadEmpresas();
      },
      error: (err) => {
        this.notification.error(err.error?.message || 'Erro ao excluir empresa');
      },
    });
  }

  openVincular(empresa: Empresa): void {
    // Reload empresa details
    this.empresaService.findById(empresa.id!).subscribe({
      next: (data) => {
        this.empresaVincular = data;
        this.fornecedorIdVincular = null;
        this.loadFornecedoresDisponiveis();
      },
    });
  }

  loadFornecedoresDisponiveis(): void {
    this.fornecedorService.findAll('', '', 0, 100).subscribe({
      next: (data) => {
        const vinculadosIds = new Set(
          this.empresaVincular?.fornecedores?.map((f) => f.id) || []
        );
        this.fornecedoresDisponiveis = data.content.filter(
          (f) => !vinculadosIds.has(f.id!)
        );
      },
    });
  }

  vincular(): void {
    if (!this.empresaVincular?.id || !this.fornecedorIdVincular) return;
    this.empresaService
      .vincularFornecedor(this.empresaVincular.id, this.fornecedorIdVincular)
      .subscribe({
        next: (data) => {
          this.empresaVincular = data;
          this.fornecedorIdVincular = null;
          this.loadFornecedoresDisponiveis();
          this.loadEmpresas(this.page?.pageNumber);
          this.notification.success('Fornecedor vinculado com sucesso');
        },
        error: (err) => {
          this.notification.error(err.error?.message || 'Erro ao vincular fornecedor');
        },
      });
  }

  desvincular(fornecedorId: number): void {
    if (!this.empresaVincular?.id) return;
    this.empresaService
      .desvincularFornecedor(this.empresaVincular.id, fornecedorId)
      .subscribe({
        next: (data) => {
          this.empresaVincular = data;
          this.loadFornecedoresDisponiveis();
          this.loadEmpresas(this.page?.pageNumber);
          this.notification.success('Fornecedor desvinculado');
        },
        error: (err) => {
          this.notification.error(err.error?.message || 'Erro ao desvincular');
        },
      });
  }
}
