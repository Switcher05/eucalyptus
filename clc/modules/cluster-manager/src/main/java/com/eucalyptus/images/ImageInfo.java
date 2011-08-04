/*******************************************************************************
 *Copyright (c) 2009  Eucalyptus Systems, Inc.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, only version 3 of the License.
 * 
 * 
 *  This file is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Please contact Eucalyptus Systems, Inc., 130 Castilian
 *  Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/>
 *  if you need additional information or have any questions.
 * 
 *  This file may incorporate work covered under the following copyright and
 *  permission notice:
 * 
 *    Software License Agreement (BSD License)
 * 
 *    Copyright (c) 2008, Regents of the University of California
 *    All rights reserved.
 * 
 *    Redistribution and use of this software in source and binary forms, with
 *    or without modification, are permitted provided that the following
 *    conditions are met:
 * 
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *      Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 * 
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *    IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 *    OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
 *    THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
 *    LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
 *    SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *    IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
 *    BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
 *    THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
 *    OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
 *    WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
 *    ANY SUCH LICENSES OR RIGHTS.
 *******************************************************************************/
/*
 * Author: chris grzegorczyk <grze@eucalyptus.com>
 */

package com.eucalyptus.images;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Entity;
import com.eucalyptus.auth.Accounts;
import com.eucalyptus.auth.principal.Account;
import com.eucalyptus.auth.principal.UserFullName;
import com.eucalyptus.cloud.Image;
import com.eucalyptus.cloud.UserMetadata;
import com.eucalyptus.component.ComponentIds;
import com.eucalyptus.component.id.Eucalyptus;
import com.eucalyptus.entities.Transactions;
import com.eucalyptus.util.FullName;
import com.eucalyptus.util.OwnerFullName;
import com.eucalyptus.util.Tx;
import com.eucalyptus.util.async.Callback;
import com.google.common.collect.Lists;

@Entity
@javax.persistence.Entity
@PersistenceContext( name = "eucalyptus_cloud" )
@Table( name = "metadata_images" )
@Cache( usage = CacheConcurrencyStrategy.TRANSACTIONAL )
@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn( name = "metadata_image_discriminator", discriminatorType = DiscriminatorType.STRING )
@DiscriminatorValue( value = "metadata_kernel_or_ramdisk" )
public class ImageInfo extends UserMetadata<Image.State> implements Image {
  
  @Transient
  private static Logger         LOG            = Logger.getLogger( ImageInfo.class );
  
  /**
   * Name of the registered image info:
   * Constraints: 3-128 alphanumeric characters, parenthesis (()), commas (,), slashes (/), dashes
   * (-), or underscores(_)
   */
  @Column( name = "metadata_image_name", nullable = false )
  private String                imageName;
  
  @Column( name = "metadata_image_description" )
  private String                description;
  
  @Column( name = "metadata_image_arch", nullable = false )
  @Enumerated( EnumType.STRING )
  private Image.Architecture    architecture;
  
  @Column( name = "metadata_image_is_public", columnDefinition = "boolean default true" )
  private Boolean               imagePublic;
  
  @Column( name = "metadata_image_platform", nullable = false )
  @Enumerated( EnumType.STRING )
  private Image.Platform        platform;
  
  @Column( name = "metadata_image_type" )
  @Enumerated( EnumType.STRING )
  private Image.Type            imageType;
  
  @OneToMany( cascade = { CascadeType.ALL }, mappedBy = "parent" )
  @Cache( usage = CacheConcurrencyStrategy.TRANSACTIONAL )
  private Set<LaunchPermission> permissions    = new HashSet<LaunchPermission>( );
  
  @OneToMany( cascade = { CascadeType.ALL }, mappedBy = "parent" )
  @Cache( usage = CacheConcurrencyStrategy.TRANSACTIONAL )
  private Set<ProductCode>      productCodes   = new HashSet<ProductCode>( );
  
  @OneToMany( cascade = { CascadeType.ALL }, mappedBy = "parent" )
  @Cache( usage = CacheConcurrencyStrategy.TRANSACTIONAL )
  private Set<DeviceMapping>    deviceMappings = new HashSet<DeviceMapping>( );
  
  @Column( name = "metadata_image_size_bytes", nullable = false )
  private Long                  imageSizeBytes;
  
  @Transient
  private FullName              fullName;
  
  public ImageInfo( ) {}
  
  public ImageInfo( final Image.Type imageType ) {
    this.imageType = imageType;
  }
  
  ImageInfo( final String imageId ) {
    this( );
    this.setDisplayName( imageId.substring( 0, 4 ).toLowerCase( ) + imageId.substring( 4 ).toUpperCase( ) );
  }
  
  ImageInfo( final Image.Type imageType, final String imageId ) {
    this( imageId );
    this.imageType = imageType;
  }
  
  protected ImageInfo( final OwnerFullName ownerFullName, final String imageId,
                       final Image.Type imageType, final String imageName, final String imageDescription, final Long imageSizeBytes,
                       final Image.Architecture arch, final Image.Platform platform ) {
    super( ownerFullName, imageId.substring( 0, 4 ).toLowerCase( ) + imageId.substring( 4 ).toUpperCase( ) );
    assertThat( imageName, notNullValue( ) );
    assertThat( imageType, notNullValue( ) );
    assertThat( imageSizeBytes, notNullValue( ) );
    assertThat( arch, notNullValue( ) );
    assertThat( platform, notNullValue( ) );
    this.setState( Image.State.pending );
    this.imageType = imageType;
    this.imageName = imageName;
    this.description = imageDescription;
    this.imageSizeBytes = imageSizeBytes;
    this.architecture = arch;
    this.platform = platform;
    this.imagePublic = ImageConfiguration.getInstance( ).getDefaultVisibility( );
  }
  
  public Image.Type getImageType( ) {
    return this.imageType;
  }
  
  public Image.Platform getPlatform( ) {
    return this.platform;
  }
  
  protected void setPlatform( final Image.Platform platform ) {
    this.platform = platform;
  }
  
  public Architecture getArchitecture( ) {
    return this.architecture;
  }
  
  protected void setArchitecture( final Architecture architecture ) {
    this.architecture = architecture;
  }
  
  public Boolean getImagePublic( ) {
    return this.imagePublic;
  }
  
  public void setImagePublic( final Boolean aPublic ) {
    this.imagePublic = aPublic;
  }
  
  private Set<LaunchPermission> getPermissions( ) {
    return permissions;
  }
  
  private void setPermissions( final Set<LaunchPermission> permissions ) {
    this.permissions = permissions;
  }
  
  @SuppressWarnings( "unchecked" )
  public ImageInfo grantPermission( final Account account ) {
    try {
      Transactions.one( new ImageInfo( this.displayName ), new Callback<ImageInfo>( ) {
        @Override
        public void fire( final ImageInfo t ) {
//          EntityWrapper<ImageInfo> db = Transactions.join( );
          LaunchPermission imgAuth = new LaunchPermission( t, account.getAccountNumber( ) );
          if ( !t.getPermissions( ).contains( imgAuth ) ) {
//            db.recast( LaunchPermission.class ).add( imgAuth );
            t.getPermissions( ).add( imgAuth );
          }
        }
      } );
    } catch ( ExecutionException e ) {
      LOG.debug( e, e );
    }
    return this;
  }
  
  public boolean checkPermission( final Account account ) {
    return true;
  }
  
  public boolean checkPermission( final String accountId ) {
    final boolean[] result = { false };
    Transactions.each( new ImageInfo( this.displayName ), new Callback<ImageInfo>( ) {
      
      @Override
      public void fire( ImageInfo t ) {
        result[0] = t.hasPermissionForOne( accountId );
      }
      
    } );
    return result[0];
  }
  
  public ImageInfo resetPermission( ) {
    try {
      Transactions.one( new ImageInfo( this.displayName ), new Tx<ImageInfo>( ) {
        @Override
        public void fire( final ImageInfo t ) throws Throwable {
          t.getPermissions( ).clear( );
          t.getPermissions( ).add( new LaunchPermission( t, t.getOwnerAccountNumber( ) ) );
          t.setImagePublic( ImageConfiguration.getInstance( ).getDefaultVisibility( ) );
        }
      } );
    } catch ( ExecutionException e ) {
      LOG.debug( e, e );
    }
    return this;
  }
  
  public List<String> listProductCodes( ) {
    final List<String> prods = Lists.newArrayList( );
    try {
      ImageInfo search = ForwardImages.exampleWithImageId( this.getDisplayName( ) );
      Transactions.one( search, new Tx<ImageInfo>( ) {
        @Override
        public void fire( final ImageInfo t ) throws Throwable {
          for ( ProductCode p : t.getProductCodes( ) ) {
            prods.add( p.getValue( ) );
          }
        }
      } );
    } catch ( ExecutionException e ) {
      LOG.debug( e, e );
    }
    return prods;
  }
  
  public List<String> listLaunchPermissions( ) {
    final List<String> perms = Lists.newArrayList( );
    try {
      ImageInfo search = ForwardImages.exampleWithImageId( this.getDisplayName( ) );
      Transactions.one( search, new Tx<ImageInfo>( ) {
        @Override
        public void fire( final ImageInfo t ) throws Throwable {
          for ( LaunchPermission p : t.getPermissions( ) ) {
            perms.add( p.getAccountId( ) );
          }
        }
      } );
    } catch ( ExecutionException e ) {
      LOG.debug( e, e );
    }
    return perms;
  }
  
  public ImageInfo resetProductCodes( ) {
    try {
      ImageInfo search = ForwardImages.exampleWithImageId( this.getDisplayName( ) );
      Transactions.one( search, new Tx<ImageInfo>( ) {
        @Override
        public void fire( final ImageInfo t ) throws Throwable {
          t.getProductCodes( ).clear( );
        }
      } );
    } catch ( ExecutionException e ) {
      LOG.debug( e, e );
    }
    return this;
  }
  
  public ImageInfo revokePermission( final Account account ) {
    try {
      ImageInfo search = ForwardImages.exampleWithImageId( this.getDisplayName( ) );
      Transactions.one( search, new Tx<ImageInfo>( ) {
        @Override
        public void fire( final ImageInfo t ) throws Throwable {
          LaunchPermission imgAuth;
          t.getPermissions( ).remove( new LaunchPermission( t, account.getAccountNumber( ) ) );
        }
      } );
    } catch ( ExecutionException e ) {
      LOG.debug( e, e );
    }
    return this;
  }
  
  private Set<ProductCode> getProductCodes( ) {
    return this.productCodes;
  }
  
  private void setProductCodes( final Set<ProductCode> productCodes ) {
    this.productCodes = productCodes;
  }
  
  /**
   * @see com.eucalyptus.util.Mappable#getName()
   * @see com.eucalyptus.cloud.UserMetadata#equals(java.lang.Object)
   * @param o
   * @return
   */
  @Override
  public boolean equals( final Object o ) {
    if ( this == o ) return true;
    if ( o == null || getClass( ) != o.getClass( ) ) return false;
    
    ImageInfo imageInfo = ( ImageInfo ) o;
    
    if ( !this.getDisplayName( ).equals( imageInfo.getDisplayName( ) ) ) return false;
    
    return true;
  }
  
  /**
   * @see com.eucalyptus.cloud.UserMetadata#hashCode()
   * @see com.eucalyptus.util.Mappable#hashCode()
   * @return
   */
  @Override
  public int hashCode( ) {
    return this.getDisplayName( ).hashCode( );
  }
  
  public boolean isAllowed( final Account account ) {
    return this.getImagePublic( ) || this.checkPermission( account );
  }
  
  /**
   * @see com.eucalyptus.util.Mappable#toString()
   * @return
   */
  @Override
  public String toString( ) {
    return this.getFullName( ).toString( );
  }
  
  @Override
  public int compareTo( final Image o ) {
    return this.getDisplayName( ).compareTo( o.getName( ) );
  }
  
  @Override
  public String getPartition( ) {
    return ComponentIds.lookup( Eucalyptus.class ).name( );
  }
  
  @Override
  public FullName getFullName( ) {
    return FullName.create.vendor( "euca" )
                          .region( ComponentIds.lookup( Eucalyptus.class ).name( ) )
                          .namespace( this.getOwnerAccountNumber( ) )
                          .relativeId( "image", this.getDisplayName( ) );
  }
  
  protected void setImageType( final Type imageType ) {
    this.imageType = imageType;
  }
  
  public Long getImageSizeBytes( ) {
    return this.imageSizeBytes;
  }
  
  protected void setImageSizeBytes( final Long imageSizeBytes ) {
    this.imageSizeBytes = imageSizeBytes;
  }
  
  public boolean addProductCode( final String prodCode ) {
    try {
      ImageInfo search = ForwardImages.exampleWithImageId( this.getDisplayName( ) );
      Transactions.one( search, new Tx<ImageInfo>( ) {
        @Override
        public void fire( final ImageInfo t ) throws Throwable {
          t.getProductCodes( ).add( new ProductCode( t, prodCode ) );
        }
      }
                  );
    } catch ( ExecutionException e ) {
      return false;
    }
    return true;
  }
  
  public String getDescription( ) {
    return this.description;
  }
  
  protected void setDescription( final String description ) {
    this.description = description;
  }
  
  public Set<DeviceMapping> getDeviceMappings( ) {
    return this.deviceMappings;
  }
  
  protected void setDeviceMappings( final Set<DeviceMapping> deviceMappings ) {
    this.deviceMappings = deviceMappings;
  }
  
  public String getImageName( ) {
    return this.imageName;
  }
  
  protected void setImageName( final String imageName ) {
    this.imageName = imageName;
  }
  
  /**
   * Can only be used within the scope of a db transaction.
   * 
   * @param accountIds
   * @return true if image has launch permission for any of the account in accountIds
   */
  public boolean hasExplicitPermissionForAny( Set<String> accountIds ) {
    final Set<LaunchPermission> permissions = getPermissions( );
    for ( String aid : accountIds ) {
      if ( permissions.contains( new LaunchPermission( this, aid ) ) ) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Can only be used within the scope of a db transaction.
   * 
   * @param accountId
   * @return true if image has launch permission including public, explicit and implicit
   */
  public boolean hasPermissionForOne( String accountId ) {
    return getImagePublic( ) || hasExplicitOrImplicitPermissionForOne( accountId );
  }
  
  /**
   * Can only be used within the scope of a db transaction.
   * 
   * @param accountId
   * @return true if image has explicit or implicit launch permission
   */
  public boolean hasExplicitOrImplicitPermissionForOne( String accountId ) {
    return getOwnerAccountNumber( ).equals( accountId ) ||
           getPermissions( ).contains( new LaunchPermission( this, accountId ) );
  }
  
  /**
   * Add launch permissions.
   * 
   * Can only be used within the scope of a db transaction.
   * 
   * @param accountIds
   */
  public void addPermissions( List<String> accountIds ) {
    for ( String aid : accountIds ) {
      try {
        // Verify account ID
        Account account = Accounts.lookupAccountById( aid );
        LaunchPermission perm = new LaunchPermission( this, account.getAccountNumber( ) );
        if ( !getPermissions( ).contains( perm ) ) {
          getPermissions( ).add( perm );
        }
      } catch ( Exception e ) {
        LOG.error( e, e );
      }
    }
  }
  
  /**
   * Remove launch permissions.
   * 
   * Can only be used within the scope of a db transaction.
   * 
   * @param accountIds
   */
  public void removePermissions( List<String> accountIds ) {
    for ( String aid : accountIds ) {
      try {
        // Verify account ID
        Account account = Accounts.lookupAccountById( aid );
        LaunchPermission perm = new LaunchPermission( this, account.getAccountNumber( ) );
        getPermissions( ).remove( perm );
      } catch ( Exception e ) {
        LOG.error( e, e );
      }
    }
  }
  
}
