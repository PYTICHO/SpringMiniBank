package com.bank.otp_bank.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import com.bank.otp_bank.db.dto.*;
import com.bank.otp_bank.db.entity.*;
import com.bank.otp_bank.db.repository.*;
import com.bank.otp_bank.db.status.*;
import com.bank.otp_bank.exception.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    @Value("${app.values.max-cards-value}") 
    private Integer maxCardsValue;


    @Transactional
    public CreateCardResponseDto create_card(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(
            () -> new NotFoundAccountException("User с email: '%s' не существует!".formatted(email))
        );
        AccountEntity account = accountRepository.findByUserId(user.getId()).orElseThrow(
            () -> new NotFoundAccountException("Счет у юзера с email: '%s' не существует!".formatted(email))
        );

        List<CardEntity> cardsList = account.getCards();


        if (cardsList != null && cardsList.size() >= maxCardsValue) {
            throw new MaxCardsException("Уже достигнуто максимальное кол-во карт: " + maxCardsValue.toString());
        }

        LocalDate expireDate = LocalDate.now().plusYears(10);
        LocalDateTime createdAt = LocalDateTime.now();


        CardEntity newCard = CardEntity.builder()
                                .cardNumber(generateCardNumber())
                                .holderName(upperHolderName(user))
                                .expireDate(expireDate)
                                .type(CardType.VIRTUAL)
                                .status(CardStatus.ACTIVE)
                                .createdAt(createdAt)
                                .account(account)
                                .build();

        CardEntity savedCard = cardRepository.save(newCard);

        return new CreateCardResponseDto(
                savedCard.getCardNumber(),
                savedCard.getHolderName(),
                savedCard.getExpireDate(),
                savedCard.getType(),
                savedCard.getStatus(),
                savedCard.getCreatedAt(),
                account.getId()
        );
    }




    @Transactional
    public TransactionResponseDto makeTransaction(String email, TransactionRequestDto request) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(
            () -> new NotFoundAccountException("User с email: '%s' не существует!".formatted(email))
        );


        AccountEntity fromAccountEntity = accountRepository.findByUserId(user.getId()).orElseThrow(
            () -> new NotFoundAccountException("Счет у юзера с email: '%s' не существует!".formatted(email))
        );
        AccountEntity toAccountEntity = null;


        // Проверяем тип перевода и делаем сам перевод
        switch (request.type()) {

            case CARD_TO_CARD:
                if (request.to() == null) {
                    throw new UniversalException("to не должен быть null при CARD_TO_CARD!", HttpStatus.BAD_REQUEST);
                }

                String toCardNumber = formatCardNumber(request.to());
                CardEntity toCardEntity = cardRepository.findByCardNumber(toCardNumber).orElseThrow(
                    () -> new InvalidCardNumberException("Карта с номером: '%s' не найдена".formatted(toCardNumber))
                );

                toAccountEntity = toCardEntity.getAccount();

                // Переводим
                transferMoney(request.amount(), fromAccountEntity, toAccountEntity);
                break;


            case PHONE_TRANSFER:
                if (request.to() == null) {
                    throw new UniversalException("to не должен быть null при PHONE_TRANSFER!", HttpStatus.BAD_REQUEST);
                }

                // Проверяем формат номера телефона
                String normalizedPhoneNumber = GlobalFunctions.normalizePhone(request.to());

                toAccountEntity = userRepository.findByPhone(normalizedPhoneNumber).orElseThrow(
                    () -> new UniversalException("Номер телефона не привязан ни к какому аккаунту!", HttpStatus.BAD_REQUEST)
                ).getAccount();

                // Переводим
                transferMoney(request.amount(), fromAccountEntity, toAccountEntity);
                break;


            case DEPOSIT:
                if (request.to() != null) {
                    throw new UniversalException("to должен быть null при DEPOSIT!", HttpStatus.BAD_REQUEST);
                }

                toAccountEntity = fromAccountEntity;
                fromAccountEntity = null;
                transferDeposit(request.amount(), toAccountEntity);
                break;
        
            default:
                throw new UniversalException("Тип перевода: '%s' пока что не поддерживается: ".formatted(request.type()), HttpStatus.BAD_REQUEST);
        }


        return saveTransaction(
            request.amount(), 
            request.currency(), 
            request.type(), 
            request.description(), 
            fromAccountEntity, 
            toAccountEntity
        );
    }

    @Transactional(readOnly = true)
    public List<RecipientSuggestionDto> getRecipients(String email) {
        return userRepository.findAll().stream()
            .filter(user -> !user.getEmail().equals(email))
            .map(user -> new RecipientSuggestionDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                Optional.ofNullable(user.getAccount())
                    .map(AccountEntity::getCards)
                    .orElse(List.of())
                    .stream()
                    .map(card -> new RecipientCardDto(
                        card.getCardNumber(),
                        card.getHolderName(),
                        card.getType(),
                        card.getStatus()
                    ))
                    .toList()
            ))
            .filter(recipient -> !recipient.cards().isEmpty())
            .toList();
    }

    @Transactional(readOnly = true)
    public MyCardsResponseDto getMyCards(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(
            () -> new NotFoundAccountException("User с email: '%s' не существует!".formatted(email))
        );

        AccountEntity account = accountRepository.findByUserId(user.getId()).orElseThrow(
            () -> new NotFoundAccountException("Счет у юзера с email: '%s' не существует!".formatted(email))
        );

        List<CreateCardResponseDto> cards = Optional.ofNullable(account.getCards())
            .orElse(List.of())
            .stream()
            .map(card -> new CreateCardResponseDto(
                card.getCardNumber(),
                card.getHolderName(),
                card.getExpireDate(),
                card.getType(),
                card.getStatus(),
                card.getCreatedAt(),
                account.getId()
            ))
            .toList();

        return new MyCardsResponseDto(account.getId(), cards);
    }

    @Transactional(readOnly = true)
    public AccountSummaryDto getAccountSummary(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(
            () -> new NotFoundAccountException("User с email: '%s' не существует!".formatted(email))
        );

        AccountEntity account = accountRepository.findByUserId(user.getId()).orElseThrow(
            () -> new NotFoundAccountException("Счет у юзера с email: '%s' не существует!".formatted(email))
        );

        int cardsCount = Optional.ofNullable(account.getCards())
            .map(List::size)
            .orElse(0);

        return new AccountSummaryDto(
            account.getId(),
            account.getAccountNumber(),
            account.getBalance(),
            account.getCurrency(),
            cardsCount
        );
    }




    // Functions
    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber;
        String result;

        for (int i = 0; i < 50; i++) {
            cardNumber = new StringBuilder();

            // первая цифра (1–9)
            cardNumber.append(random.nextInt(9) + 1);

            // остальные 15 цифр (0–9)
            for (int j = 1; j < 16; j++) {
                cardNumber.append(random.nextInt(10));
            }

            result = cardNumber.toString()
                    .replaceAll("(.{4})", "$1 ")
                    .trim();

            if (!cardRepository.existsByCardNumber(result)) {
                return result;
            }
        }
    
        throw new IllegalStateException("Не удалось сгенерировать уникальный номер карты");
    }

    private String upperHolderName(UserEntity userEntity) {
        String fullName = (userEntity.getFirstName() + " " + userEntity.getLastName()).toUpperCase();

        return fullName;
    }

    private String formatCardNumber(String input) {
        // Убираем всё кроме цифр
        String digits = input.replaceAll("\\D", "");
    
        if (digits.length() != 16) {
            throw new InvalidCardNumberFormatException("Card number must contain 16 digits");
        }
    
        // Разбиваем на группы по 4
        return digits.replaceAll("(.{4})", "$1 ").trim();
    }




    private void transferMoney(
        BigDecimal amount, 
        AccountEntity fromAccountEntity, 
        AccountEntity toAccountEntity
    ) {
        if (toAccountEntity != null) {
            if (fromAccountEntity.equals(toAccountEntity)) {
                throw new UniversalException("Вы не можете перевести деньги самому себе", HttpStatus.BAD_REQUEST);
            }
            // Если не хватает денег для перевода у отправителя
            if (fromAccountEntity.getBalance().compareTo(amount) < 0) {
                throw new UnderFundedException("Недостаточно средств на счету отправителя!");
            }

            // Делаем сам перевод и сохраняем в БД
            fromAccountEntity.setBalance(fromAccountEntity.getBalance().subtract(amount));
            toAccountEntity.setBalance(toAccountEntity.getBalance().add(amount));
            accountRepository.save(fromAccountEntity);
            accountRepository.save(toAccountEntity);
        }
    }

    private void transferDeposit(
        BigDecimal amount,
        AccountEntity accountEntity
    ) {
        if (accountEntity == null) {
            throw new UniversalException("Ошибка на сервере: 'transferDeposit'", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        accountEntity.setBalance(accountEntity.getBalance().add(amount));
        accountRepository.save(accountEntity);
    }

    private TransactionResponseDto saveTransaction(
        BigDecimal amount,
        CurrencyStatus currency,
        TransactionType type,
        String description,
        AccountEntity fromAccountEntity,
        AccountEntity toAccountEntity
    ) {
        LocalDateTime created_at = LocalDateTime.now();
        TransactionEntity transactionEntity = TransactionEntity.builder()
                                                .amount(amount)
                                                .currency(currency)
                                                .type(type)
                                                .status(TransactionStatus.SUCCESS)
                                                .description(description)
                                                .createdAt(created_at)
                                                .fromAccount(fromAccountEntity)
                                                .toAccount(toAccountEntity)
                                                .build();
        
        TransactionEntity savedTransaction = transactionRepository.save(transactionEntity);

        return new TransactionResponseDto(
            savedTransaction.getAmount(),
            savedTransaction.getCurrency(),
            savedTransaction.getType(),
            savedTransaction.getStatus(),
            savedTransaction.getDescription(),
            savedTransaction.getCreatedAt(),
            Optional.ofNullable(savedTransaction.getFromAccount())
                .map(AccountEntity::getId)
                .orElse(null),
            Optional.ofNullable(savedTransaction.getToAccount())
                .map(AccountEntity::getId)
                .orElse(null)
        );
    }


}
