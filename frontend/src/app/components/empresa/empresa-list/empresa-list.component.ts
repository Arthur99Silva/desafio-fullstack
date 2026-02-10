import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, switchMap, catchError, of, finalize } from 'rxjs';
import { EmpresaService } from '../../../services/empresa.service';
import { FornecedorService } from '../../../services/fornecedor.service';
import { NotificationService } from '../../../services/notification.service';
import { Empresa, Fornecedor, PageResponse } from '../../../models';
import { CpfCnpjPipe } from '../../../pipes/cpf-cnpj.pipe';
import { CepFormatPipe } from '../../../pipes/cep-format.pipe';

@Component({
  selector: 'app-empresa-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, ReactiveFormsModule, CpfCnpjPipe, CepFormatPipe],
  templateUrl: './empresa-list.component.html',
})
export class EmpresaListComponent implements OnInit {
  page: PageResponse<Empresa> | null = null;
  loading = false;
  
  searchControl = new FormControl('');

  empresaToDelete: Empresa | null = null;
  empresaVincular: Empresa | null = null;
  fornecedoresDisponiveis: Fornecedor[] = [];
  fornecedorIdVincular: number | null = null;

  Math = Math;

  constructor(
    private empresaService: EmpresaService,
    private fornecedorService: FornecedorService,
    private notification: NotificationService
  ) {

    this.setupSearch();
  }

  ngOnInit(): void {
    this.loadEmpresas();
  }

  private setupSearch(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        switchMap((term) => {
          this.loading = true;
          return this.empresaService.findAll(term || '', 0).pipe(
            catchError(err => {
              this.notification.error('Erro ao pesquisar empresas');
              return of(null);
            }),
            finalize(() => this.loading = false)
          );
        }),
        takeUntilDestroyed()
      )
      .subscribe((data) => {
        if (data) {
          this.page = data;
        }
      });
  }

  loadEmpresas(page = 0): void {
    this.loading = true;
    const searchTerm = this.searchControl.value || '';
    
    this.empresaService.findAll(searchTerm, page).subscribe({
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
        this.loadEmpresas(this.page?.pageNumber || 0);
      },
      error: (err) => {
        this.notification.error(err.error?.message || 'Erro ao excluir empresa');
      },
    });
  }

  openVincular(empresa: Empresa): void {
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