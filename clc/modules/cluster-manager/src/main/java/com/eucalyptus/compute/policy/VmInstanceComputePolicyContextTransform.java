/*************************************************************************
 * Copyright 2009-2013 Eucalyptus Systems, Inc.
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
package com.eucalyptus.compute.policy;

import static com.eucalyptus.compute.policy.ComputePolicyContext.ComputePolicyContextResource;
import static com.eucalyptus.compute.policy.ComputePolicyContext.ComputePolicyContextResourceSupport;
import javax.annotation.Nullable;
import com.eucalyptus.util.TypeMapper;
import com.eucalyptus.compute.common.internal.vm.VmInstance;
import com.google.common.base.Function;

/**
 *
 */
@TypeMapper
public class VmInstanceComputePolicyContextTransform implements Function<VmInstance,ComputePolicyContextResource> {

  @Override
  public ComputePolicyContextResource apply( final VmInstance input ) {
    return new ComputePolicyContextResourceSupport( ) {
      @Nullable
      @Override
      public String getAvailabilityZone( ) {
        return input.getPartition( );
      }

      @Nullable
      @Override
      public Boolean isEbsOptimized( ) {
        return input.isEbsOptimized( );
      }

      @Nullable
      @Override
      public String getInstanceProfileArn() {
        return input.getIamInstanceProfileArn( );
      }

      @Nullable
      @Override
      public String getInstanceType() {
        return input.getInstanceType( );
      }

      @Nullable
      @Override
      public String getRootDeviceType() {
        return input.isBlockStorage( ) ? "ebs" : "instance-store";
      }

      @Nullable
      @Override
      public String getTenancy() {
        return "default";
      }
    };
  }
}
