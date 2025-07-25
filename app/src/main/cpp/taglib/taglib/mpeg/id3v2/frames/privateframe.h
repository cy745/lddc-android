/***************************************************************************
    copyright            : (C) 2008 by Serkan Kalyoncu
    copyright            : (C) 2008 by Scott Wheeler
    email                : wheeler@kde.org
***************************************************************************/

/***************************************************************************
 *   This library is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Lesser General Public License version   *
 *   2.1 as published by the Free Software Foundation.                     *
 *                                                                         *
 *   This library is distributed in the hope that it will be useful, but   *
 *   WITHOUT ANY WARRANTY; without even the implied warranty of            *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with this library; if not, write to the Free Software   *
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA         *
 *   02110-1301  USA                                                       *
 *                                                                         *
 *   Alternatively, this file is available under the Mozilla Public        *
 *   License Version 1.1.  You may obtain a copy of the License at         *
 *   http://www.mozilla.org/MPL/                                           *
 ***************************************************************************/

#ifndef TAGLIB_PRIVATEFRAME_H
#define TAGLIB_PRIVATEFRAME_H

#include "taglib_export.h"
#include "id3v2frame.h"

namespace TagLib {

  namespace ID3v2 {

    //! An implementation of ID3v2 privateframe

    class TAGLIB_EXPORT PrivateFrame : public Frame
    {
      friend class FrameFactory;

    public:
      /*!
       * Construct an empty private frame.
       */
      PrivateFrame();

      /*!
       * Construct a private frame based on the data in \a data.
       *
       * \note This is the constructor used when parsing the frame from a file.
       */
      explicit PrivateFrame(const ByteVector &data);

      /*!
       * Destroys this private frame instance.
       */
      ~PrivateFrame() override;

      PrivateFrame(const PrivateFrame &) = delete;
      PrivateFrame &operator=(const PrivateFrame &) = delete;

      /*!
       * Returns the text of this private frame, currently just the owner.
       *
       * \see text()
       */
      String toString() const override;

      /*!
       * \return The owner of the private frame.
       * \note This should contain an email address or link to a website.
       */
      String owner() const;

      /*!
       * Returns the private data.
       */
      ByteVector data() const;

      /*!
       * Sets the owner of the frame to \a s.
       * \note This should contain an email address or link to a website.
       */
      void setOwner(const String &s);

      /*!
       * Sets the private \a data.
       */
      void setData(const ByteVector &data);

    protected:
      // Reimplementations.

      void parseFields(const ByteVector &data) override;
      ByteVector renderFields() const override;

    private:
      /*!
       * The constructor used by the FrameFactory.
       */
      PrivateFrame(const ByteVector &data, Header *h);

      class PrivateFramePrivate;
      TAGLIB_MSVC_SUPPRESS_WARNING_NEEDS_TO_HAVE_DLL_INTERFACE
      std::unique_ptr<PrivateFramePrivate> d;
    };

  }  // namespace ID3v2
}  // namespace TagLib
#endif
