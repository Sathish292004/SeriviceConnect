CREATE TABLE payments (
                          id UUID PRIMARY KEY,

                          booking_id UUID NOT NULL,
                          user_id UUID NOT NULL,

                          amount NUMERIC(12, 2) NOT NULL,
                          currency VARCHAR(3) NOT NULL DEFAULT 'INR',

                          status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                          payment_method VARCHAR(30),

                          razorpay_order_id VARCHAR(255),
                          razorpay_payment_id VARCHAR(255),
                          razorpay_signature VARCHAR(500),

                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT chk_payment_amount
                              CHECK (amount > 0),

                          CONSTRAINT chk_payment_status
                              CHECK (
                                  status IN (
                                             'PENDING',
                                             'SUCCESS',
                                             'FAILED',
                                             'REFUNDED',
                                             'CANCELLED'
                                      )
                                  )
);

CREATE UNIQUE INDEX uk_payments_razorpay_order_id
    ON payments (razorpay_order_id)
    WHERE razorpay_order_id IS NOT NULL;

CREATE UNIQUE INDEX uk_payments_razorpay_payment_id
    ON payments (razorpay_payment_id)
    WHERE razorpay_payment_id IS NOT NULL;

CREATE INDEX idx_payments_booking_id
    ON payments (booking_id);

CREATE INDEX idx_payments_user_id
    ON payments (user_id);

CREATE INDEX idx_payments_status
    ON payments (status);