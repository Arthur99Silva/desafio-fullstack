import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { EmpresaService } from '../../../services/empresa.service';
import { CepService } from '../../../services/cep.service';
import { NotificationService } from '../../../services/notification.service';
import { EmpresaRequest, CepInfo } from '../../../models';

@Component({
  selector: 'app-empresa-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './empresa-form.component.html',
})
export class EmpresaFormComponent implements OnInit {
  @ViewChild('form') form!: NgForm;

  empresa: EmpresaRequest = { cnpj: '', nomeFantasia: '', cep: '' };

  isEdit = false;
  editId: number | null = null;
  submitting = false;
  cepInfo: CepInfo | null = null;
  cepError = '';
  cepValidado = false;
  consultandoCep = false;

  constructor(
    private empresaService: EmpresaService,
    private cepService: CepService,
    private notification: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.editId = +id;
      this.carregarEmpresa(this.editId);
    }
  }

  carregarEmpresa(id: number): void {
    this.empresaService.findById(id).subscribe({
      next: (data) => {
        this.empresa = {
          cnpj: data.cnpj,
          nomeFantasia: data.nomeFantasia,
          cep: data.cep,
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
        this.notification.error('Empresa não encontrada');
        this.router.navigate(['/empresas']);
      },
    });
  }

  consultarCep(): void {
    if (!this.empresa.cep || this.empresa.cep.length !== 8) return;

    this.consultandoCep = true;
    this.cepError = '';
    this.cepInfo = null;
    this.cepValidado = false;

    this.cepService.consultar(this.empresa.cep)
      .pipe(finalize(() => this.consultandoCep = false))
      .subscribe({
        next: (info) => {
          if (info.valido) {
            this.cepInfo = info;
            this.cepValidado = true;
          } else {
            this.cepError = info.mensagem || 'CEP inválido';
          }
        },
        error: () => {
          this.cepError = 'Erro ao consultar CEP';
        },
      });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.notification.warning('Preencha todos os campos obrigatórios');
      return;
    }

    if (!this.cepValidado) {
      this.notification.warning('Valide o CEP antes de salvar');
      return;
    }

    this.submitting = true;

    const request$ = this.isEdit
      ? this.empresaService.update(this.editId!, this.empresa)
      : this.empresaService.create(this.empresa);

    request$
      .pipe(
        finalize(() => this.submitting = false)
      )
      .subscribe({
        next: () => {
          this.notification.success(
            this.isEdit ? 'Empresa atualizada com sucesso' : 'Empresa cadastrada com sucesso'
          );
          this.router.navigate(['/empresas']);
        },
        error: (err) => {
          this.notification.error(err.error?.message || 'Erro ao salvar empresa');
        },
      });
  }
}