import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { FornecedorService } from '../../../services/fornecedor.service';
import { CepService } from '../../../services/cep.service';
import { NotificationService } from '../../../services/notification.service';
import { FornecedorRequest, CepInfo } from '../../../models';

@Component({
  selector: 'app-fornecedor-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './fornecedor-form.component.html',
})
export class FornecedorFormComponent implements OnInit {
  fornecedor: FornecedorRequest = {
    cpfCnpj: '',
    tipoPessoa: 'JURIDICA',
    nome: '',
    email: '',
    cep: '',
    rg: '',
    dataNascimento: '',
  };

  isEdit = false;
  editId: number | null = null;
  submitting = false;

  cepInfo: CepInfo | null = null;
  cepError = '';
  cepValidado = false;
  consultandoCep = false;

  constructor(
    private fornecedorService: FornecedorService,
    private cepService: CepService,
    private notification: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.editId = +id;
      this.fornecedorService.findById(this.editId).subscribe({
        next: (data) => {
          this.fornecedor = {
            cpfCnpj: data.cpfCnpj,
            tipoPessoa: data.tipoPessoa,
            nome: data.nome,
            email: data.email,
            cep: data.cep,
            rg: data.rg || '',
            dataNascimento: data.dataNascimento || '',
          };
          if (data.uf) {
            this.cepInfo = {
              cep: data.cep,
              uf: data.uf,
              cidade: data.cidade || '',
              bairro: data.bairro || '',
              logradouro: data.logradouro || '',
              valido: true,
            };
            this.cepValidado = true;
          }
        },
        error: () => {
          this.notification.error('Fornecedor não encontrado');
          this.router.navigate(['/fornecedores']);
        },
      });
    }
  }

  onTipoPessoaChange(): void {
    this.fornecedor.cpfCnpj = '';
    if (this.fornecedor.tipoPessoa === 'JURIDICA') {
      this.fornecedor.rg = '';
      this.fornecedor.dataNascimento = '';
    }
  }

  consultarCep(): void {
    if (!this.fornecedor.cep || this.fornecedor.cep.length !== 8) return;

    this.consultandoCep = true;
    this.cepError = '';
    this.cepInfo = null;
    this.cepValidado = false;

    this.cepService.consultar(this.fornecedor.cep).subscribe({
      next: (info) => {
        if (info.valido) {
          this.cepInfo = info;
          this.cepValidado = true;
        } else {
          this.cepError = info.mensagem || 'CEP inválido';
        }
        this.consultandoCep = false;
      },
      error: () => {
        this.cepError = 'Erro ao consultar CEP';
        this.consultandoCep = false;
      },
    });
  }

  onSubmit(): void {
    if (!this.cepValidado) {
      this.notification.warning('Valide o CEP antes de salvar');
      return;
    }

    this.submitting = true;

    const payload: FornecedorRequest = { ...this.fornecedor };
    if (payload.tipoPessoa === 'JURIDICA') {
      delete payload.rg;
      delete payload.dataNascimento;
    }

    const obs = this.isEdit
      ? this.fornecedorService.update(this.editId!, payload)
      : this.fornecedorService.create(payload);

    obs.subscribe({
      next: () => {
        this.notification.success(
          this.isEdit
            ? 'Fornecedor atualizado com sucesso'
            : 'Fornecedor cadastrado com sucesso'
        );
        this.router.navigate(['/fornecedores']);
      },
      error: (err) => {
        this.notification.error(
          err.error?.message || 'Erro ao salvar fornecedor'
        );
        this.submitting = false;
      },
    });
  }
}
