import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'empresas', pathMatch: 'full' },
  {
    path: 'empresas',
    loadComponent: () =>
      import('./components/empresa/empresa-list/empresa-list.component').then(
        (m) => m.EmpresaListComponent
      ),
  },
  {
    path: 'empresas/novo',
    loadComponent: () =>
      import('./components/empresa/empresa-form/empresa-form.component').then(
        (m) => m.EmpresaFormComponent
      ),
  },
  {
    path: 'empresas/editar/:id',
    loadComponent: () =>
      import('./components/empresa/empresa-form/empresa-form.component').then(
        (m) => m.EmpresaFormComponent
      ),
  },
  {
    path: 'fornecedores',
    loadComponent: () =>
      import(
        './components/fornecedor/fornecedor-list/fornecedor-list.component'
      ).then((m) => m.FornecedorListComponent),
  },
  {
    path: 'fornecedores/novo',
    loadComponent: () =>
      import(
        './components/fornecedor/fornecedor-form/fornecedor-form.component'
      ).then((m) => m.FornecedorFormComponent),
  },
  {
    path: 'fornecedores/editar/:id',
    loadComponent: () =>
      import(
        './components/fornecedor/fornecedor-form/fornecedor-form.component'
      ).then((m) => m.FornecedorFormComponent),
  },
  { path: '**', redirectTo: 'empresas' },
];
