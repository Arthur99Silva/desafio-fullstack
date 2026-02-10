import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FornecedorService } from '../../../services/fornecedor.service';
import { NotificationService } from '../../../services/notification.service';
import { Fornecedor, PageResponse } from '../../../models';
import { CpfCnpjPipe } from '../../../pipes/cpf-cnpj.pipe';
import { CepFormatPipe } from '../../../pipes/cep-format.pipe';

@Component({
  selector: 'app-fornecedor-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, CpfCnpjPipe],
  templateUrl: './fornecedor-list.component.html',
})
export class FornecedorListComponent implements OnInit {
  page: PageResponse<Fornecedor> | null = null;
  filterNome = '';
  filterCpfCnpj = '';
  loading = false;
  filterTimeout: any;
  fornecedorToDelete: Fornecedor | null = null;

  Math = Math;

  constructor(
    private fornecedorService: FornecedorService,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadFornecedores();
  }

  loadFornecedores(page = 0): void {
    this.loading = true;
    this.fornecedorService
      .findAll(this.filterNome, this.filterCpfCnpj, page)
      .subscribe({
        next: (data) => {
          this.page = data;
          this.loading = false;
        },
        error: () => {
          this.notification.error('Erro ao carregar fornecedores');
          this.loading = false;
        },
      });
  }

  onFilter(): void {
    clearTimeout(this.filterTimeout);
    this.filterTimeout = setTimeout(() => this.loadFornecedores(), 400);
  }

  goToPage(page: number): void {
    this.loadFornecedores(page);
  }

  confirmDelete(f: Fornecedor): void {
    this.fornecedorToDelete = f;
  }

  deleteFornecedor(): void {
    if (!this.fornecedorToDelete?.id) return;
    this.fornecedorService.delete(this.fornecedorToDelete.id).subscribe({
      next: () => {
        this.notification.success('Fornecedor excluído com sucesso');
        this.fornecedorToDelete = null;
        this.loadFornecedores();
      },
      error: (err) => {
        this.notification.error(err.error?.message || 'Erro ao excluir fornecedor');
      },
    });
  }
}
