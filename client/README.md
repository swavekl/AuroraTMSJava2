# Client

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 7.0.6.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `npm run e2e` to execute the end-to-end tests with Playwright. The E2E server uses HTTPS on port 4200 and the Angular proxy configuration. The routine tests mock registration APIs so they do not create Okta users or send email. Run `npm run e2e:ui` to use Playwright's interactive test runner.

The live registration test is skipped unless `LIVE_REGISTRATION_ENABLED=true`, `LIVE_REGISTRATION_EMAIL`, and `LIVE_REGISTRATION_PASSWORD` are set in the environment. It submits a real registration, then exercises two resend requests and Start Over. This creates a staged Okta account and sends an initial verification email plus two resend emails. Use an unused test address and set credentials in the shell or IDE run configuration rather than in source files.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
