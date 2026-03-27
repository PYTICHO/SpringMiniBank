import { useEffect, useMemo, useRef, useState } from 'react';
import { clearSession, createApi, loadSession, saveSession } from './api';

const currencyOptions = ['RUB', 'USD', 'CNY'];

const navItems = [
  { id: 'overview', label: 'Главная', icon: 'home' },
  { id: 'payments', label: 'Платежи', icon: 'send' },
  { id: 'cards', label: 'Карты', icon: 'card' },
  { id: 'security', label: 'Безопасность', icon: 'shield' }
];

const initialRegisterForm = {
  firstName: '',
  lastName: '',
  phone: '',
  email: '',
  password: ''
};

const initialLoginForm = {
  email: '',
  password: ''
};

const initialDepositForm = {
  amount: '1000',
  currency: 'RUB',
  description: 'Демо-пополнение'
};

const initialCardTransferForm = {
  amount: '100',
  currency: 'RUB',
  description: 'Перевод по карте',
  to: ''
};

const initialPhoneTransferForm = {
  amount: '100',
  currency: 'RUB',
  description: 'Перевод по телефону',
  to: ''
};

function App() {
  const [session, setSession] = useState(loadSession());
  const sessionRef = useRef(session);
  const [authMode, setAuthMode] = useState('login');
  const [activeSection, setActiveSection] = useState('overview');
  const [registerForm, setRegisterForm] = useState(initialRegisterForm);
  const [loginForm, setLoginForm] = useState(initialLoginForm);
  const [depositForm, setDepositForm] = useState(initialDepositForm);
  const [cardTransferForm, setCardTransferForm] = useState(initialCardTransferForm);
  const [phoneTransferForm, setPhoneTransferForm] = useState(initialPhoneTransferForm);
  const [status, setStatus] = useState('Ожидаем вход в защищенный кабинет');
  const [error, setError] = useState('');
  const [loadingKey, setLoadingKey] = useState('');
  const [authCheck, setAuthCheck] = useState('');
  const [cards, setCards] = useState([]);
  const [activity, setActivity] = useState([]);
  const [recipients, setRecipients] = useState([]);
  const [recipientsError, setRecipientsError] = useState('');
  const [cardsError, setCardsError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    sessionRef.current = session;
  }, [session]);

  const api = useMemo(
    () =>
      createApi(
        () => sessionRef.current,
        (nextSession) => setSession(nextSession),
        () => {
          setStatus('Сессия завершена. Войдите снова.');
          setError('Refresh token истек или стал невалиден.');
          setAuthMode('login');
        }
      ),
    []
  );

  function rememberActivity(title, payload) {
    setActivity((current) => [
      {
        id: crypto.randomUUID(),
        title,
        timestamp: new Date().toLocaleString('ru-RU'),
        payload
      },
      ...current
    ].slice(0, 10));
  }

  function updateForm(setter, name, value) {
    setter((current) => ({
      ...current,
      [name]: value
    }));
  }

  function persistAuth(payload, label) {
    const nextSession = {
      ...payload,
      receivedAt: new Date().toISOString()
    };

    saveSession(nextSession);
    setSession(nextSession);
    setStatus(label);
    setError('');
    setSuccessMessage('');
    setAuthCheck('');
    setActiveSection('overview');
    rememberActivity(label, {
      userId: payload.userId,
      email: payload.email,
      refreshTokenExpiresAt: payload.refreshTokenExpiresAt
    });
  }

  async function runAction(key, action) {
    setLoadingKey(key);
    setError('');
    setSuccessMessage('');

    try {
      await action();
    } catch (err) {
      const message = err?.payload?.message || err.message || 'Неизвестная ошибка';
      setError(message);
      setStatus('Операция завершилась ошибкой');
    } finally {
      setLoadingKey('');
    }
  }

  function buildMoneyPayload(form, type) {
    return {
      amount: Number(form.amount),
      currency: form.currency,
      type,
      description: form.description,
      to: form.to || null
    };
  }

  useEffect(() => {
    if (!session?.accessToken) {
      setRecipients([]);
      setRecipientsError('');
      setCards([]);
      setCardsError('');
      return;
    }

    let ignore = false;

    async function loadProtectedData() {
      try {
        const [recipientsPayload, myCardsPayload] = await Promise.all([
          api.getRecipients(),
          api.getMyCards()
        ]);

        if (!ignore) {
          setRecipients(recipientsPayload);
          setRecipientsError('');
          setCards(myCardsPayload.cards || []);
          setCardsError('');
        }
      } catch (err) {
        if (!ignore) {
          setRecipients([]);
          setCards([]);
          setRecipientsError('Не удалось загрузить список получателей. Обновите токены или перезапустите backend.');
          setCardsError('Не удалось загрузить ваши карты. Обновите токены или перезапустите backend.');
        }
      }
    }

    loadProtectedData();

    return () => {
      ignore = true;
    };
  }, [api, session?.accessToken]);

  async function handleRegister(event) {
    event.preventDefault();
    await runAction('register', async () => {
      const payload = await api.register(registerForm);
      persistAuth(payload, 'Регистрация выполнена. Сессия открыта.');
      setRegisterForm(initialRegisterForm);
    });
  }

  async function handleLogin(event) {
    event.preventDefault();
    await runAction('login', async () => {
      const payload = await api.login(loginForm);
      persistAuth(payload, 'Вход выполнен. Сессия восстановлена.');
      setLoginForm(initialLoginForm);
    });
  }

  async function handleRefresh() {
    if (!session?.refreshToken) {
      setError('У текущей сессии нет refresh token.');
      return;
    }

    await runAction('refresh', async () => {
      const payload = await api.refresh(session.refreshToken);
      if (payload) {
        setStatus('Токены обновлены через refresh token.');
        rememberActivity('Refresh token использован', {
          email: payload.email,
          refreshTokenExpiresAt: payload.refreshTokenExpiresAt
        });
      }
    });
  }

  async function handleCheckAuth() {
    await runAction('checkAuth', async () => {
      const payload = await api.checkAuth();
      const message = typeof payload === 'string' ? payload : payload.message;
      setAuthCheck(message || JSON.stringify(payload));
      setStatus('Сервер подтвердил авторизацию.');
      rememberActivity('Проверка авторизации', payload);
    });
  }

  async function handleCreateCard() {
    await runAction('createCard', async () => {
      const payload = await api.createCard();
      setCards((current) => [payload, ...current]);
      setCardsError('');
      setStatus('Новая виртуальная карта выпущена.');
      rememberActivity('Выпуск карты', payload);
      setActiveSection('cards');
      const nextRecipients = await api.getRecipients();
      setRecipients(nextRecipients);
    });
  }

  async function handleDeposit(event) {
    event.preventDefault();
    await runAction('deposit', async () => {
      const payload = await api.makeDeposit(buildMoneyPayload(depositForm, 'DEPOSIT'));
      setStatus('Пополнение счета прошло успешно.');
      rememberActivity('Пополнение счета', payload);
    });
  }

  async function handleCardTransfer(event) {
    event.preventDefault();
    await runAction('cardTransfer', async () => {
      const payload = await api.makeTransaction(
        buildMoneyPayload(cardTransferForm, 'CARD_TO_CARD')
      );
      setStatus('Перевод по номеру карты выполнен.');
      setSuccessMessage('Перевод выполнен!');
      rememberActivity('Перевод по карте', payload);
    });
  }

  async function handlePhoneTransfer(event) {
    event.preventDefault();
    await runAction('phoneTransfer', async () => {
      const payload = await api.makeTransaction(
        buildMoneyPayload(phoneTransferForm, 'PHONE_TRANSFER')
      );
      setStatus('Перевод по номеру телефона выполнен.');
      setSuccessMessage('Перевод выполнен!');
      rememberActivity('Перевод по телефону', payload);
    });
  }

  function handleLogout() {
    clearSession();
    setSession(null);
    setAuthCheck('');
    setCards([]);
    setActivity([]);
    setRecipients([]);
    setRecipientsError('');
    setCardsError('');
    setStatus('Сессия очищена на клиенте.');
    setError('');
    setSuccessMessage('');
    setAuthMode('login');
  }

  const filteredRecipients = recipients.filter((recipient) => {
    const query = cardTransferForm.to.trim().toLowerCase();

    if (!query) {
      return true;
    }

    const fullName = `${recipient.firstName} ${recipient.lastName}`.toLowerCase();
    return (
      fullName.includes(query) ||
      recipient.phone.toLowerCase().includes(query) ||
      recipient.cards.some((card) => card.cardNumber.toLowerCase().includes(query))
    );
  });

  if (!session?.accessToken) {
    return (
      <div className="auth-page">
        <section className="auth-brand">
          <div className="otp-logo">
            <span className="otp-mark" />
            <strong>Kasay Bank</strong>
          </div>

          <div className="auth-showcase">
            <h1>Kasay Bank</h1>
            <p className="hero-copy">Вход в личный кабинет</p>
          </div>
        </section>

        <section className="auth-card">
          <div className="auth-toggle">
            <button
              className={authMode === 'login' ? 'tab active' : 'tab'}
              onClick={() => setAuthMode('login')}
              type="button"
            >
              Вход
            </button>
            <button
              className={authMode === 'register' ? 'tab active' : 'tab'}
              onClick={() => setAuthMode('register')}
              type="button"
            >
              Регистрация
            </button>
          </div>

          {authMode === 'login' ? (
            <form className="auth-form" onSubmit={handleLogin}>
              <h2>Вход в кабинет</h2>
              <div className="form-grid single-column">
                <input
                  value={loginForm.email}
                  onChange={(e) => updateForm(setLoginForm, 'email', e.target.value)}
                  placeholder="mail@example.com"
                  type="email"
                  required
                />
                <input
                  value={loginForm.password}
                  onChange={(e) => updateForm(setLoginForm, 'password', e.target.value)}
                  placeholder="Пароль"
                  type="password"
                  minLength="6"
                  required
                />
              </div>
              <button className="primary-button" disabled={loadingKey === 'login'} type="submit">
                {loadingKey === 'login' ? 'Входим...' : 'Продолжить'}
              </button>
            </form>
          ) : (
            <form className="auth-form" onSubmit={handleRegister}>
              <h2>Создать аккаунт</h2>
              <div className="form-grid">
                <input
                  value={registerForm.firstName}
                  onChange={(e) => updateForm(setRegisterForm, 'firstName', e.target.value)}
                  placeholder="Имя"
                  required
                />
                <input
                  value={registerForm.lastName}
                  onChange={(e) => updateForm(setRegisterForm, 'lastName', e.target.value)}
                  placeholder="Фамилия"
                  required
                />
                <input
                  value={registerForm.phone}
                  onChange={(e) => updateForm(setRegisterForm, 'phone', e.target.value)}
                  placeholder="+7 999 123-45-67"
                  required
                />
                <input
                  value={registerForm.email}
                  onChange={(e) => updateForm(setRegisterForm, 'email', e.target.value)}
                  placeholder="mail@example.com"
                  type="email"
                  required
                />
                <input
                  value={registerForm.password}
                  onChange={(e) => updateForm(setRegisterForm, 'password', e.target.value)}
                  placeholder="Пароль"
                  type="password"
                  minLength="6"
                  required
                />
              </div>
              <button className="primary-button" disabled={loadingKey === 'register'} type="submit">
                {loadingKey === 'register' ? 'Создаем...' : 'Открыть кабинет'}
              </button>
            </form>
          )}

          <div className="status-box">
            <span className="muted-label">Статус</span>
            <strong>{status}</strong>
            {error ? <p className="error-text">{error}</p> : null}
          </div>
        </section>
      </div>
    );
  }

  return (
    <div className="bank-shell">
      <aside className="sidebar">
        <div className="otp-logo">
          <span className="otp-mark" />
          <strong>Kasay</strong>
        </div>

        <div className="profile-card">
          <div className="avatar-circle">{session.firstName?.slice(0, 1)}</div>
          <div>
            <strong>{session.firstName}</strong>
            <strong>{session.lastName}</strong>
            <span>{session.email}</span>
          </div>
        </div>

        <nav className="side-nav">
          {navItems.map((item) => (
            <button
              className={activeSection === item.id ? 'nav-button active' : 'nav-button'}
              key={item.id}
              onClick={() => setActiveSection(item.id)}
              type="button"
            >
              <Icon name={item.icon} />
              <span>{item.label}</span>
            </button>
          ))}
        </nav>

      </aside>

      <main className="workspace">
        <header className="workspace-header">
          <div>
            <h1>{getSectionTitle(activeSection)}</h1>
          </div>
          <div className="header-actions">
            <button className="ghost-button" onClick={handleRefresh} type="button">
              Обновить токены
            </button>
            <button className="primary-button small" onClick={handleLogout} type="button">
              Выход
            </button>
          </div>
        </header>

        {activeSection === 'overview' ? (
          <section className="content-grid">
            <article className="hero-card wide-card">
              <div className="hero-card-top">
                <div>
                  <h2>Добро пожаловать</h2>
                </div>
              </div>
              <p className="headline-copy">Все основные операции доступны в меню слева.</p>

              <div className="quick-actions">
                <button className="action-chip" onClick={handleCreateCard} type="button">
                  <span>+</span>
                  Выпустить карту
                </button>
                <button
                  className="action-chip"
                  onClick={() => setActiveSection('payments')}
                  type="button"
                >
                  <span>→</span>
                  Перейти к платежам
                </button>
                <button
                  className="action-chip"
                  onClick={() => setActiveSection('security')}
                  type="button"
                >
                  <span>•</span>
                  Проверить сессию
                </button>
              </div>
            </article>

            <article className="info-card">
              <p className="muted-label">Пользователь</p>
              <h3>
                {session.firstName} {session.lastName}
              </h3>
              <span>{session.phone}</span>
              <span>{session.email}</span>
            </article>

            <article className="panel-card">
              <div className="card-head">
                <div>
                  <h3>Быстрые действия</h3>
                </div>
              </div>
              <div className="feature-list">
                <div>Выпуск карты</div>
                <div>Пополнение</div>
                <div>Перевод по карте</div>
                <div>Перевод по телефону</div>
              </div>
            </article>

            <article className="panel-card">
              <div className="card-head">
                <div>
                  <h3>Последние операции</h3>
                </div>
              </div>
              <div className="activity-feed">
                {activity.length === 0 ? (
                  <div className="empty-state">После первых действий ответы сервера появятся здесь.</div>
                ) : (
                  activity.slice(0, 4).map((item) => (
                    <article className="activity-item" key={item.id}>
                      <div className="result-topline">
                        <strong>{item.title}</strong>
                        <span>{item.timestamp}</span>
                      </div>
                      <pre>{JSON.stringify(item.payload, null, 2)}</pre>
                    </article>
                  ))
                )}
              </div>
            </article>
          </section>
        ) : null}

        {activeSection === 'payments' ? (
          <section className="payments-layout">
            <article className="panel-card payment-stage">
              <div className="card-head">
                <div>
                  <p className="muted-label">По номеру карты</p>
                  <h3>Перевод</h3>
                </div>
              </div>
              <form className="bank-form" onSubmit={handleCardTransfer}>
                <MoneyFields
                  form={cardTransferForm}
                  setForm={setCardTransferForm}
                  allowTarget
                  targetExtras={
                    <RecipientPicker
                      recipients={filteredRecipients}
                      recipientsError={recipientsError}
                      onSelectCard={(cardNumber) =>
                        setCardTransferForm((current) => ({
                          ...current,
                          to: cardNumber
                        }))
                      }
                    />
                  }
                />
                <button className="primary-button block" disabled={loadingKey === 'cardTransfer'} type="submit">
                  {loadingKey === 'cardTransfer' ? 'Отправляем...' : 'Продолжить'}
                </button>
                {successMessage ? <div className="success-banner">{successMessage}</div> : null}
              </form>
            </article>

            <article className="panel-card payment-stage">
              <div className="card-head">
                <div>
                  <p className="muted-label">По номеру телефона</p>
                  <h3>Перевод</h3>
                </div>
              </div>
              <form className="bank-form" onSubmit={handlePhoneTransfer}>
                <MoneyFields
                  form={phoneTransferForm}
                  setForm={setPhoneTransferForm}
                  allowTarget
                  targetPlaceholder="+7 999 123-45-67"
                />
                <button className="primary-button block" disabled={loadingKey === 'phoneTransfer'} type="submit">
                  {loadingKey === 'phoneTransfer' ? 'Отправляем...' : 'Продолжить'}
                </button>
                {successMessage ? <div className="success-banner">{successMessage}</div> : null}
              </form>
            </article>

            <article className="panel-card payment-stage">
              <div className="card-head">
                <div>
                  <h3>Демо-депозит</h3>
                </div>
              </div>
              <form className="bank-form" onSubmit={handleDeposit}>
                <MoneyFields form={depositForm} setForm={setDepositForm} allowTarget={false} />
                <button className="primary-button block" disabled={loadingKey === 'deposit'} type="submit">
                  {loadingKey === 'deposit' ? 'Пополняем...' : 'Пополнить счет'}
                </button>
              </form>
            </article>
          </section>
        ) : null}

        {activeSection === 'cards' ? (
          <section className="content-grid">
            <article className="panel-card wide-card">
              <div className="card-head">
                <div>
                  <h3>Виртуальные карты</h3>
                </div>
                <button className="primary-button small" onClick={handleCreateCard} type="button">
                  Выпустить
                </button>
              </div>

              <div className="cards-grid">
                {cardsError ? (
                  <div className="empty-state error-text">{cardsError}</div>
                ) : cards.length === 0 ? (
                  <div className="empty-state">Карт пока нет. Выпустите первую виртуальную карту.</div>
                ) : (
                  cards.map((card) => (
                    <article className="bank-card" key={card.cardNumber}>
                      <div className="bank-card-brand">Kasay Bank</div>
                      <strong>{card.cardNumber}</strong>
                      <span>{card.holderName}</span>
                      <div className="bank-card-meta">
                        <small>{card.type}</small>
                        <small>{card.status}</small>
                      </div>
                      <small>Истекает: {card.expireDate}</small>
                    </article>
                  ))
                )}
              </div>
            </article>
          </section>
        ) : null}

        {activeSection === 'security' ? (
          <section className="content-grid">
            <article className="panel-card">
              <div className="card-head">
                <div>
                  <h3>Параметры авторизации</h3>
                </div>
              </div>

              <div className="security-grid">
                <div className="detail-row">
                  <span>Email</span>
                  <strong>{session.email}</strong>
                </div>
                <div className="detail-row">
                  <span>Телефон</span>
                  <strong>{session.phone}</strong>
                </div>
                <div className="detail-row">
                  <span>Refresh expires</span>
                  <strong>{session.refreshTokenExpiresAt}</strong>
                </div>
              </div>

              <div className="button-row">
                <button className="primary-button small" onClick={handleCheckAuth} type="button">
                  Проверить авторизацию
                </button>
                <button className="ghost-button" onClick={handleRefresh} type="button">
                  Обновить refresh
                </button>
              </div>
            </article>

            <article className="panel-card">
              <div className="card-head">
                <div>
                  <h3>Статус</h3>
                </div>
              </div>
              <div className="status-box tight">
                <strong>{status}</strong>
                {authCheck ? <p>{authCheck}</p> : null}
                {error ? <p className="error-text">{error}</p> : null}
              </div>
            </article>
          </section>
        ) : null}
      </main>
    </div>
  );
}

function MoneyFields({
  form,
  setForm,
  allowTarget,
  targetPlaceholder = '0000 0000 0000 0000',
  targetExtras = null
}) {
  return (
    <div className="bank-form-grid">
      <label className="field">
        <span>Сумма</span>
        <input
          value={form.amount}
          onChange={(e) => setForm((current) => ({ ...current, amount: e.target.value }))}
          placeholder="От 10 до 500 000"
          min="0.01"
          step="0.01"
          type="number"
          required
        />
      </label>

      <label className="field">
        <span>Валюта</span>
        <select
          value={form.currency}
          onChange={(e) => setForm((current) => ({ ...current, currency: e.target.value }))}
        >
          {currencyOptions.map((currency) => (
            <option key={currency} value={currency}>
              {currency}
            </option>
          ))}
        </select>
      </label>

      <label className="field wide">
        <span>Описание</span>
        <input
          value={form.description}
          onChange={(e) => setForm((current) => ({ ...current, description: e.target.value }))}
          placeholder="Назначение перевода"
        />
      </label>

      {allowTarget ? (
        <label className="field wide">
          <span>Куда</span>
          <input
            value={form.to}
            onChange={(e) => setForm((current) => ({ ...current, to: e.target.value }))}
            placeholder={targetPlaceholder}
            required
          />
          {targetExtras}
        </label>
      ) : null}
    </div>
  );
}

function RecipientPicker({ recipients, recipientsError, onSelectCard }) {
  if (recipientsError) {
    return <div className="picker-note error-text">{recipientsError}</div>;
  }

  if (recipients.length === 0) {
    return (
      <div className="picker-note">
        Пока нет доступных пользователей с выпущенными картами для перевода.
      </div>
    );
  }

  return (
    <div className="recipient-picker">
      <div className="picker-head">
        <span>Получатели</span>
        <small>Нажмите на карту, чтобы подставить номер</small>
      </div>
      <div className="recipient-list">
        {recipients.map((recipient) => (
          <article className="recipient-item" key={recipient.userId}>
            <div className="recipient-user">
              <strong>
                {recipient.firstName} {recipient.lastName}
              </strong>
              <span>
                {recipient.phone} • {recipient.email}
              </span>
            </div>
            <div className="recipient-cards">
              {recipient.cards.map((card) => (
                <button
                  className="recipient-card-chip"
                  key={card.cardNumber}
                  onClick={() => onSelectCard(card.cardNumber)}
                  type="button"
                >
                  <strong>{card.cardNumber}</strong>
                  <span>
                    {card.type} • {card.status}
                  </span>
                </button>
              ))}
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}

function Icon({ name }) {
  if (name === 'home') {
    return <span className="nav-icon">⌂</span>;
  }

  if (name === 'send') {
    return <span className="nav-icon">↗</span>;
  }

  if (name === 'card') {
    return <span className="nav-icon">▣</span>;
  }

  return <span className="nav-icon">◌</span>;
}

function getSectionTitle(section) {
  if (section === 'payments') {
    return 'Платежи и переводы';
  }

  if (section === 'cards') {
    return 'Счета и карты';
  }

  if (section === 'security') {
    return 'Безопасность';
  }

  return 'Главная';
}

export default App;
