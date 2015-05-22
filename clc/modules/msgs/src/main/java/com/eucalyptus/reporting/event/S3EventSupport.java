/*************************************************************************
 * Copyright 2009-2015 Eucalyptus Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Please contact Eucalyptus Systems, Inc., 6755 Hollister Ave., Goleta
 * CA 93117, USA or visit http://www.eucalyptus.com/licenses/ if you need
 * additional information or have any questions.
 ************************************************************************/
package com.eucalyptus.reporting.event;

import static com.eucalyptus.util.Parameters.checkParam;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import javax.annotation.Nonnull;
import com.eucalyptus.event.Event;

/**
 * Support class for S3 events
 */
class S3EventSupport<E extends Enum<E>> implements Event {
  private static final long serialVersionUID = 1L;

  private final E action;
  private final String userId;
  private final String userName;
  private final String accountNumber;
  private final Long size;
  private final String bucketName;

  S3EventSupport( @Nonnull final E action,
                  @Nonnull final String bucketName,
                  @Nonnull final String userId,
                  @Nonnull final String userName,
                  @Nonnull final String accountNumber,
                  @Nonnull final Long size ) {
    checkParam( action, notNullValue() );
    checkParam( bucketName, not( isEmptyOrNullString() ) );
    checkParam( userId, not( isEmptyOrNullString() ) );
    checkParam( userName, not( isEmptyOrNullString() ) );
    checkParam( accountNumber, not( isEmptyOrNullString() ) );
    checkParam( size, notNullValue() );

    this.action = action;
    this.userId = userId;
    this.userName = userName;
    this.accountNumber = accountNumber;
    this.size = size;
    this.bucketName = bucketName;
  }

  @Nonnull
  public E getAction() {
    return action;
  }

  @Nonnull
  public String getUserId() {
    return userId;
  }

  @Nonnull
  public String getUserName() {
    return userName;
  }

  @Nonnull
  public String getAccountNumber() {
    return accountNumber;
  }

  @Nonnull
  public Long getSize() {
    return size;
  }

  @Nonnull
  public String getBucketName() {
    return bucketName;
  }
}
